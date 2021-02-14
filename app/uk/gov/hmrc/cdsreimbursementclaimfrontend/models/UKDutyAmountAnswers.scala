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

package uk.gov.hmrc.cdsreimbursementclaimfrontend.models

import julienrf.json.derived
import play.api.libs.json.OFormat
import uk.gov.hmrc.cdsreimbursementclaimfrontend.controllers.claims.EnterDutyAmountsController.EnterClaim

sealed trait UKDutyAmountAnswers extends Product with Serializable

object UKDutyAmountAnswers {

  final case class IncompleteUKDutyAmountAnswer(
    ukDutyAmounts: List[EnterClaim]
  ) extends UKDutyAmountAnswers

  object IncompleteUKDutyAmountAnswer {
    val empty: IncompleteUKDutyAmountAnswer = IncompleteUKDutyAmountAnswer(List.empty)

    implicit val format: OFormat[IncompleteUKDutyAmountAnswer] =
      derived.oformat[IncompleteUKDutyAmountAnswer]()
  }

  final case class CompleteUKDutyAmountAnswer(
    ukDutyAmounts: List[EnterClaim]
  ) extends UKDutyAmountAnswers

  object CompleteMovementReferenceTypeAnswer {
    implicit val format: OFormat[CompleteUKDutyAmountAnswer] =
      derived.oformat[CompleteUKDutyAmountAnswer]()
  }

  implicit class ClaimantDetailsAsImporterCompanyAnswerOps(
    private val a: UKDutyAmountAnswers
  ) extends AnyVal {

    def fold[A](
      ifIncomplete: IncompleteUKDutyAmountAnswer => A,
      ifComplete: CompleteUKDutyAmountAnswer => A
    ): A =
      a match {
        case i: IncompleteUKDutyAmountAnswer => ifIncomplete(i)
        case c: CompleteUKDutyAmountAnswer   => ifComplete(c)
      }
  }

  implicit val format: OFormat[UKDutyAmountAnswers] =
    derived.oformat[UKDutyAmountAnswers]()
}
