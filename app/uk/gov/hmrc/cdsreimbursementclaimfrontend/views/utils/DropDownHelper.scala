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

package uk.gov.hmrc.cdsreimbursementclaimfrontend.views.utils

import javax.inject.Inject
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.cdsreimbursementclaimfrontend.utils.HtmlUtil
import uk.gov.hmrc.cdsreimbursementclaimfrontend.views.html.components.paragraph_block
import uk.gov.hmrc.cdsreimbursementclaimfrontend.views.html.components.bullets

class DropDownHelper @Inject() (paragraph: paragraph_block, bulletList: bullets) extends HtmlUtil {

  val landingKey: String          = "landing.details"
  val supportingEvidenceUploadKey = "supporting-evidence.upload.details"
  val reasonAndBasisBasisKey      = "select-reason-and-basis-for-claim.basis.details"
  val reasonAndBasisReasonKey     = "select-reason-and-basis-for-claim.reason.details"
  val basisKey                    = "select-basis-for-claim.details"
  val whoIsMakingTheClaimKey      = "select-who-is-making-the-claim.details"

  def landingDropDownContent()(implicit messages: Messages): Html =
    html(
      paragraph(Html(messages(s"$landingKey.p1")), Some("govuk-body")),
      bulletList(
        Seq(
          Html(messages(s"$landingKey.l1")),
          Html(messages(s"$landingKey.l2")),
          Html(messages(s"$landingKey.l3")),
          Html(messages(s"$landingKey.l4")),
          Html(messages(s"$landingKey.l5")),
          Html(messages(s"$landingKey.l6")),
          Html(messages(s"$landingKey.l7")),
          Html(messages(s"$landingKey.l8")),
          Html(messages(s"$landingKey.l9")),
          Html(messages(s"$landingKey.l10"))
        )
      )
    )

  def uploadDropDownContent()(implicit messages: Messages): Html =
    html(
      bulletList(
        Seq(
          Html(messages(s"$supportingEvidenceUploadKey.l0")),
          Html(messages(s"$supportingEvidenceUploadKey.l1")),
          Html(messages(s"$supportingEvidenceUploadKey.l2")),
          Html(messages(s"$supportingEvidenceUploadKey.l3")),
          Html(messages(s"$supportingEvidenceUploadKey.l4")),
          Html(messages(s"$supportingEvidenceUploadKey.l5")),
          Html(messages(s"$supportingEvidenceUploadKey.l6")),
          Html(messages(s"$supportingEvidenceUploadKey.l8")),
          Html(messages(s"$supportingEvidenceUploadKey.l9")),
          Html(messages(s"$supportingEvidenceUploadKey.l10"))
        )
      )
    )

  def reasonAndBasisBasisDropDownContent()(implicit messages: Messages): Html =
    html(
      bulletList(
        Seq(
          Html(messages(s"$reasonAndBasisBasisKey.l0")),
          Html(messages(s"$reasonAndBasisBasisKey.l1")),
          Html(messages(s"$reasonAndBasisBasisKey.l2")),
          Html(messages(s"$reasonAndBasisBasisKey.l3")),
          Html(messages(s"$reasonAndBasisBasisKey.l4")),
          Html(messages(s"$reasonAndBasisBasisKey.l5")),
          Html(messages(s"$reasonAndBasisBasisKey.l6")),
          Html(messages(s"$reasonAndBasisBasisKey.l7")),
          Html(messages(s"$reasonAndBasisBasisKey.l8")),
          Html(messages(s"$reasonAndBasisBasisKey.l9"))
        )
      )
    )

  def reasonAndBasisReasonDropDownContent()(implicit messages: Messages): Html =
    html(
      bulletList(
        Seq(
          Html(messages(s"$reasonAndBasisReasonKey.l0")),
          Html(messages(s"$reasonAndBasisReasonKey.l1")),
          Html(messages(s"$reasonAndBasisReasonKey.l2"))
        )
      )
    )

  def basisDropDownContent()(implicit messages: Messages): Html =
    html(
      bulletList(
        Seq(
          Html(messages(s"$basisKey.l0")),
          Html(messages(s"$basisKey.l1")),
          Html(messages(s"$basisKey.l2")),
          Html(messages(s"$basisKey.l3")),
          Html(messages(s"$basisKey.l4")),
          Html(messages(s"$basisKey.l5")),
          Html(messages(s"$basisKey.l6")),
          Html(messages(s"$basisKey.l7")),
          Html(messages(s"$basisKey.l8")),
          Html(messages(s"$basisKey.l9")),
          Html(messages(s"$basisKey.l10"))
        )
      )
    )

  def whoIsDropDownContent()(implicit messages: Messages): Html =
    html(
      bulletList(
        Seq(
          Html(messages(s"$whoIsMakingTheClaimKey.l0")),
          Html(messages(s"$whoIsMakingTheClaimKey.l1")),
          Html(messages(s"$whoIsMakingTheClaimKey.l2"))
        )
      )
    )

}
