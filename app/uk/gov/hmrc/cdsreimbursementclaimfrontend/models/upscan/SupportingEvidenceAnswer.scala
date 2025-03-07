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

package uk.gov.hmrc.cdsreimbursementclaimfrontend.models.upscan

import julienrf.json.derived
import play.api.libs.json.{Json, OFormat}

sealed trait SupportingEvidenceAnswer extends Product with Serializable

object SupportingEvidenceAnswer {

  final case class IncompleteSupportingEvidenceAnswer(
    evidences: List[SupportingEvidence]
  ) extends SupportingEvidenceAnswer

  object IncompleteSupportingEvidenceAnswer {
    val empty: IncompleteSupportingEvidenceAnswer =
      IncompleteSupportingEvidenceAnswer(List.empty)
  }

  final case class CompleteSupportingEvidenceAnswer(
    evidences: List[SupportingEvidence]
  ) extends SupportingEvidenceAnswer

  object CompleteSupportingEvidenceAnswer {
    implicit val format: OFormat[CompleteSupportingEvidenceAnswer] = Json.format[CompleteSupportingEvidenceAnswer]
  }

  implicit class SupportingEvidenceAnswerOps(
    private val a: SupportingEvidenceAnswer
  ) extends AnyVal {

    def fold[A](
      ifIncomplete: IncompleteSupportingEvidenceAnswer => A,
      ifComplete: CompleteSupportingEvidenceAnswer => A
    ): A =
      a match {
        case i: IncompleteSupportingEvidenceAnswer => ifIncomplete(i)
        case c: CompleteSupportingEvidenceAnswer   => ifComplete(c)
      }
  }

  implicit val format: OFormat[SupportingEvidenceAnswer] = derived.oformat()
}
