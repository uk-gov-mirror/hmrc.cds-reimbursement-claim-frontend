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

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.cdsreimbursementclaimfrontend.cache.SessionCache
import uk.gov.hmrc.cdsreimbursementclaimfrontend.controllers.{AuthSupport, ControllerSpec, SessionSupport, routes => baseRoutes}
import uk.gov.hmrc.cdsreimbursementclaimfrontend.models.DraftClaim.DraftC285Claim
import uk.gov.hmrc.cdsreimbursementclaimfrontend.models.JourneyStatus.FillingOutClaim
import uk.gov.hmrc.cdsreimbursementclaimfrontend.models._
import uk.gov.hmrc.cdsreimbursementclaimfrontend.models.email.Email
import uk.gov.hmrc.cdsreimbursementclaimfrontend.models.generators.EmailGen._
import uk.gov.hmrc.cdsreimbursementclaimfrontend.models.generators.Generators.sample
import uk.gov.hmrc.cdsreimbursementclaimfrontend.models.generators.IdGen._
import uk.gov.hmrc.cdsreimbursementclaimfrontend.models.ids.GGCredId

import scala.concurrent.Future

class CheckEoriDetailsControllerSpec
    extends ControllerSpec
    with AuthSupport
    with SessionSupport
    with ScalaCheckDrivenPropertyChecks {

  override val overrideBindings: List[GuiceableModule] =
    List[GuiceableModule](
      bind[AuthConnector].toInstance(mockAuthConnector),
      bind[SessionCache].toInstance(mockSessionCache)
    )

  lazy val controller: CheckEoriDetailsController = instanceOf[CheckEoriDetailsController]

  implicit lazy val messagesApi: MessagesApi = controller.messagesApi

  implicit lazy val messages: Messages = MessagesImpl(Lang("en"), messagesApi)

  private def sessionWithClaimState(): (SessionData, FillingOutClaim, DraftC285Claim) = {
    val draftC285Claim      = DraftC285Claim.newDraftC285Claim
    val ggCredId            = sample[GGCredId]
    val email               = sample[Email]
    val eori                = sample[Eori]
    val signedInUserDetails =
      SignedInUserDetails(Some(email), eori, Email("amina@email.com"), ContactName("Fred Bread"))
    val journey             = FillingOutClaim(ggCredId, signedInUserDetails, draftC285Claim)
    (
      SessionData.empty.copy(journeyStatus = Some(journey)),
      journey,
      draftC285Claim
    )
  }

  "Check Eori Details Controller" must {

    "redirect to the start of the journey" when {
      "there is no journey status in the session" in {
        def performAction(): Future[Result] = controller.show()(FakeRequest())
        val (session, _, _)                 = sessionWithClaimState()

        inSequence {
          mockAuthWithNoRetrievals()
          mockGetSession(session.copy(journeyStatus = None))
        }

        checkIsRedirect(
          performAction(),
          baseRoutes.StartController.start()
        )
      }
    }

    "Render the page" when {
      "The user is logged in" in {
        def performAction(): Future[Result] = controller.show()(FakeRequest())

        val (session, fillingOutClaim, _) = sessionWithClaimState()

        inSequence {
          mockAuthWithNoRetrievals()
          mockGetSession(session.copy(journeyStatus = Some(fillingOutClaim)))
        }

        checkPageIsDisplayed(
          performAction(),
          messageFromMessageKey("check-eori-details.title")
        )
      }
    }

    "Handle submissions" when {

      def performAction(data: Seq[(String, String)]): Future[Result] =
        controller.submit()(FakeRequest().withFormUrlEncodedBody(data: _*))

      "The user chooses the yes option" in {
        val (session, fillingOutClaim, _) = sessionWithClaimState()

        inSequence {
          mockAuthWithNoRetrievals()
          mockGetSession(session.copy(journeyStatus = Some(fillingOutClaim)))
        }

        val result = performAction(Seq(CheckEoriDetailsController.dataKey -> "0"))
        checkIsRedirect(result, routes.EnterMovementReferenceNumberController.enterMrn())
      }

      "The user chooses the Eori is incorrect, logout option" in {
        val (session, fillingOutClaim, _) = sessionWithClaimState()

        inSequence {
          mockAuthWithNoRetrievals()
          mockGetSession(session.copy(journeyStatus = Some(fillingOutClaim)))
        }

        val result = performAction(Seq(CheckEoriDetailsController.dataKey -> "1"))
        checkIsRedirect(result, viewConfig.ggSignOut)
      }

      "The user submits an invalid choice" in {
        val (session, fillingOutClaim, _) = sessionWithClaimState()

        inSequence {
          mockAuthWithNoRetrievals()
          mockGetSession(session.copy(journeyStatus = Some(fillingOutClaim)))
        }

        val result = performAction(Seq(CheckEoriDetailsController.dataKey -> "2"))

        checkPageIsDisplayed(
          result,
          messageFromMessageKey("check-eori-details.title"),
          _.select("#check-eori-details-error").text() shouldBe "Error: " + messageFromMessageKey(
            s"check-eori-details.invalid"
          ),
          BAD_REQUEST
        )
      }

      "The user submits no choice" in {
        val (session, fillingOutClaim, _) = sessionWithClaimState()

        inSequence {
          mockAuthWithNoRetrievals()
          mockGetSession(session.copy(journeyStatus = Some(fillingOutClaim)))
        }

        val result = performAction(Seq.empty)

        checkPageIsDisplayed(
          result,
          messageFromMessageKey("check-eori-details.title"),
          _.select("#check-eori-details-error").text() shouldBe "Error: " + messageFromMessageKey(
            s"check-eori-details.invalid"
          ),
          BAD_REQUEST
        )
      }

    }

  }
}
