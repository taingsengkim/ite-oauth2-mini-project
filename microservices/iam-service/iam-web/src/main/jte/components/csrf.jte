@import kh.edu.istad.platform.iam.web.view.WebUi
@param WebUi ui
<input type="hidden" name="${ui.csrfName()}" value="${ui.csrfToken()}">
