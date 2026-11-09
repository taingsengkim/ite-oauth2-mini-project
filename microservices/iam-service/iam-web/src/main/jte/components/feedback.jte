@import kh.edu.istad.platform.iam.web.view.WebUi
@param WebUi ui
@param String error = null
@param String success = null
@if(error != null)
    <div class="notice notice-error" role="alert"><span class="notice-dot" aria-hidden="true"></span>${ui.t(error)}</div>
@endif
@if(success != null)
    <div class="notice notice-success" role="status"><span class="notice-dot" aria-hidden="true"></span>${ui.t(success)}</div>
@endif
