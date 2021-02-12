/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.cdsreimbursementclaimfrontend.controllers.claims

import cats.data.EitherT
import com.google.inject.{Inject, Singleton}
import play.api.data.Forms.{mapping, nonEmptyText, of}
import play.api.data.{Form, Mapping}
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.cdsreimbursementclaimfrontend.cache.SessionCache
import uk.gov.hmrc.cdsreimbursementclaimfrontend.config.{ErrorHandler, ViewConfig}
import uk.gov.hmrc.cdsreimbursementclaimfrontend.controllers.actions.{AuthenticatedAction, RequestWithSessionData, SessionDataAction, WithAuthAndSessionDataAction}
import uk.gov.hmrc.cdsreimbursementclaimfrontend.controllers.{SessionUpdates, routes => baseRoutes}
import uk.gov.hmrc.cdsreimbursementclaimfrontend.models.DeclarantDetailAnswers.IncompleteDeclarationDetailAnswer
import uk.gov.hmrc.cdsreimbursementclaimfrontend.models.JourneyStatus.FillingOutClaim
import uk.gov.hmrc.cdsreimbursementclaimfrontend.models.email.Email
import uk.gov.hmrc.cdsreimbursementclaimfrontend.models._
import uk.gov.hmrc.cdsreimbursementclaimfrontend.util.toFuture
import uk.gov.hmrc.cdsreimbursementclaimfrontend.utils.Logging._
import uk.gov.hmrc.cdsreimbursementclaimfrontend.utils.{Logging, TimeUtils}
import uk.gov.hmrc.cdsreimbursementclaimfrontend.views.html.{claims => pages}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnterDeclarationDetailsController @Inject() (
  val authenticatedAction: AuthenticatedAction,
  val sessionDataAction: SessionDataAction,
  val sessionStore: SessionCache,
  val errorHandler: ErrorHandler,
  cc: MessagesControllerComponents,
  enterDeclarationDetailsPage: pages.enter_declaration_details
)(implicit ec: ExecutionContext, viewConfig: ViewConfig)
    extends FrontendController(cc)
    with WithAuthAndSessionDataAction
    with SessionUpdates
    with Logging {

  private def withDeclarationDetails(
    f: (
      SessionData,
      FillingOutClaim,
      DeclarantDetailAnswers
    ) => Future[Result]
  )(implicit request: RequestWithSessionData[_]): Future[Result] =
    request.sessionData.flatMap(s => s.journeyStatus.map(s -> _)) match {
      case Some(
            (
              sessionData,
              fillingOutClaim @ FillingOutClaim(_, _, draftClaim: DraftClaim)
            )
          ) =>
        val maybeDeclarationAnswers = draftClaim.fold(
          _.declarationDetailAnswers
        )
        maybeDeclarationAnswers.fold[Future[Result]](
          f(sessionData, fillingOutClaim, IncompleteDeclarationDetailAnswer.empty)
        )(f(sessionData, fillingOutClaim, _))
      case _ => Redirect(baseRoutes.StartController.start())
    }

  def enterDeclarationDetails(): Action[AnyContent] = authenticatedActionWithSessionData.async { implicit request =>
    withDeclarationDetails { (_, fillingOutClaim, answers) =>
      answers.fold(
        ifIncomplete =>
          ifIncomplete.entryDeclaration match {
            case Some(reference) =>
              fillingOutClaim.draftClaim
                .movementReferenceNumber()
                .fold(Redirect(routes.EnterMovementReferenceNumberController.enterMrn())) {
                  case Left(value) =>
                    Ok(
                      enterDeclarationDetailsPage(
                        EnterDeclarationDetailsController.entryDeclarationDetailsForm.fill(reference),
                        value,
                        routes.EnterMovementReferenceNumberController.enterMrn()
                      )
                    )
                  case Right(_)    => Redirect(routes.EnterMovementReferenceNumberController.enterMrn())
                }
            case None            =>
              fillingOutClaim.draftClaim
                .movementReferenceNumber()
                .fold(Redirect(routes.EnterMovementReferenceNumberController.enterMrn())) {
                  case Left(value) =>
                    Ok(
                      enterDeclarationDetailsPage(
                        EnterDeclarationDetailsController.entryDeclarationDetailsForm,
                        value,
                        routes.EnterMovementReferenceNumberController.enterMrn()
                      )
                    )
                  case Right(_)    => Redirect(routes.EnterMovementReferenceNumberController.enterMrn())
                }
          },
        ifComplete =>
          fillingOutClaim.draftClaim
            .movementReferenceNumber()
            .fold(Redirect(routes.EnterMovementReferenceNumberController.enterMrn())) {
              case Left(value) =>
                Ok(
                  enterDeclarationDetailsPage(
                    EnterDeclarationDetailsController.entryDeclarationDetailsForm.fill(ifComplete.entryDeclaration),
                    value,
                    routes.EnterMovementReferenceNumberController.enterMrn()
                  )
                )
              case Right(_)    => Redirect(routes.EnterMovementReferenceNumberController.enterMrn())
            }
      )
    }
  }

  def enterDeclarationDetailsSubmit(): Action[AnyContent] =
    authenticatedActionWithSessionData.async { implicit request =>
      withDeclarationDetails { (_, fillingOutClaim, answers) =>
        EnterDeclarationDetailsController.entryDeclarationDetailsForm
          .bindFromRequest()
          .fold(
            requestFormWithErrors =>
              fillingOutClaim.draftClaim
                .movementReferenceNumber()
                .fold(Redirect(routes.EnterMovementReferenceNumberController.enterMrn())) {
                  case Left(value) =>
                    BadRequest(
                      enterDeclarationDetailsPage(
                        requestFormWithErrors,
                        value,
                        routes.EnterMovementReferenceNumberController.enterMrn()
                      )
                    )
                  case Right(_)    => Redirect(routes.EnterMovementReferenceNumberController.enterMrn())
                },
            declarantDetailAnswers => {
              val updatedAnswers = answers.fold(
                incomplete =>
                  incomplete.copy(
                    entryDeclaration = Some(declarantDetailAnswers)
                  ),
                complete => complete.copy(entryDeclaration = declarantDetailAnswers)
              )
              val newDraftClaim  =
                fillingOutClaim.draftClaim.fold(_.copy(declarationDetailAnswers = Some(updatedAnswers)))

              val updatedJourney = fillingOutClaim.copy(draftClaim = newDraftClaim)

              val result = EitherT
                .liftF(updateSession(sessionStore, request)(_.copy(journeyStatus = Some(updatedJourney))))
                .leftMap((_: Unit) => Error("could not update session"))

              result.fold(
                e => {
                  logger.warn("could not capture declaration details", e)
                  errorHandler.errorResult()
                },
                _ => Redirect(routes.WhoIsMakingTheClaimController.chooseDeclarantType())
              )
            }
          )
      }
    }
}

object EnterDeclarationDetailsController {

  final case class EntryDeclarationDetails(
    dateOfImport: DateOfImport,
    placeOfImport: String,
    importerName: String,
    importerEmailAddress: Email,
    importerPhoneNumber: String,
    declarantName: String,
    declarantEmailAddress: Email,
    declarantPhoneNumber: String
  )

  object EntryDeclarationDetails {
    implicit val format: OFormat[EntryDeclarationDetails] = Json.format[EntryDeclarationDetails]
  }

  val entryDeclarationDetailsForm: Form[EntryDeclarationDetails] = Form(
    mapping(
      "enter-declaration-details.date-of-import"          -> acquisitionDateForm(LocalDate.now),
      "enter-declaration-details.place-of-import"         -> nonEmptyText,
      "enter-declaration-details.importer-name"           -> nonEmptyText,
      "enter-declaration-details.importer-email-address"  -> Email.mapping,
      "enter-declaration-details.importer-phone-number"   -> nonEmptyText,
      "enter-declaration-details.declarant-name"          -> nonEmptyText,
      "enter-declaration-details.declarant-email-address" -> Email.mapping,
      "enter-declaration-details.declarant-phone-number"  -> nonEmptyText
    )(EntryDeclarationDetails.apply)(EntryDeclarationDetails.unapply)
  )

  def acquisitionDateForm(today: LocalDate): Mapping[DateOfImport] =
    mapping(
      "" -> of(
        TimeUtils.dateFormatter(
          Some(today),
          None,
          "enter-declaration-details-day",
          "enter-declaration-details-month",
          "enter-declaration-details-year",
          "enter-declaration-details"
        )
      )
    )(DateOfImport(_))(d => Some(d.value))

}
