const en = {
  title: 'Login History', description: 'Review successful and failed sign-in attempts associated with your account.',
  loading: 'Loading login history…', failed: 'Could not load login history. Try again.', expired: 'Your session expired. Sign in again.', signIn: 'Sign in again',
  result: 'Result', all: 'All results', success: 'Successful', failure: 'Failed', from: 'From (local time)', to: 'To (local time)',
  suspiciousOnly: 'Suspicious activity only', suspicious: 'Flagged for review', apply: 'Apply filters', reset: 'Reset', refresh: 'Refresh',
  invalidDate: 'Enter a valid date and time.', invalidRange: 'To must be after or equal to From.',
  empty: 'No sign-in attempts match these filters.', total: '{count} matching events', page: 'Page {page} of {pages}', previous: 'Previous', next: 'Next',
  ip: 'IP address', browser: 'Browser / device', rawAgent: 'Browser-reported details', unknown: 'Unavailable', password: 'Password',
  historyHint: 'Newest first. Only attempts associated with your account are shown; unidentified attempts are excluded. History begins when login recording was enabled.',
  suspiciousHint: 'Five or more failures within ten minutes for an account, login, or IP are flagged for review. Shared networks can produce false positives; this does not prove account compromise.',
  deviceHint: 'Browser and device information is estimated from the user-agent string.',
}
export const historyMessages: Record<'en' | 'km', typeof en> = { en, km: {
  title: 'ប្រវត្តិចូលគណនី', description: 'ពិនិត្យការព្យាយាមចូលដែលជោគជ័យ និងបរាជ័យដែលទាក់ទងនឹងគណនីរបស់អ្នក។',
  loading: 'កំពុងផ្ទុកប្រវត្តិចូលគណនី…', failed: 'មិនអាចផ្ទុកប្រវត្តិចូលគណនីបានទេ។ សូមព្យាយាមម្ដងទៀត។', expired: 'វគ្គរបស់អ្នកបានផុតកំណត់។ សូមចូលម្ដងទៀត។', signIn: 'ចូលម្ដងទៀត',
  result: 'លទ្ធផល', all: 'លទ្ធផលទាំងអស់', success: 'ជោគជ័យ', failure: 'បរាជ័យ', from: 'ចាប់ពី (ម៉ោងមូលដ្ឋាន)', to: 'ដល់ (ម៉ោងមូលដ្ឋាន)',
  suspiciousOnly: 'តែសកម្មភាពគួរឱ្យសង្ស័យ', suspicious: 'បានសម្គាល់សម្រាប់ពិនិត្យ', apply: 'អនុវត្តតម្រង', reset: 'កំណត់ឡើងវិញ', refresh: 'ផ្ទុកឡើងវិញ',
  invalidDate: 'សូមបញ្ចូលកាលបរិច្ឆេទ និងម៉ោងត្រឹមត្រូវ។', invalidRange: 'ម៉ោងបញ្ចប់ត្រូវក្រោយ ឬស្មើម៉ោងចាប់ផ្ដើម។',
  empty: 'មិនមានការព្យាយាមចូលដែលត្រូវនឹងតម្រងទាំងនេះទេ។', total: 'ព្រឹត្តិការណ៍ដែលត្រូវគ្នា {count}', page: 'ទំព័រ {page} នៃ {pages}', previous: 'មុន', next: 'បន្ទាប់',
  ip: 'អាសយដ្ឋាន IP', browser: 'កម្មវិធីរុករក / ឧបករណ៍', rawAgent: 'ព័ត៌មានដែលកម្មវិធីរុករករាយការណ៍', unknown: 'មិនមានព័ត៌មាន', password: 'ពាក្យសម្ងាត់',
  historyHint: 'បង្ហាញថ្មីបំផុតមុន។ បង្ហាញតែការព្យាយាមដែលទាក់ទងនឹងគណនីរបស់អ្នក។ ការព្យាយាមដែលមិនអាចកំណត់អត្តសញ្ញាណមិនត្រូវបានបង្ហាញទេ។ ប្រវត្តិចាប់ផ្ដើមនៅពេលបើកការកត់ត្រាចូល។',
  suspiciousHint: 'ការបរាជ័យយ៉ាងតិចប្រាំដងក្នុងដប់នាទី សម្រាប់គណនី ឈ្មោះចូល ឬ IP ត្រូវបានសម្គាល់សម្រាប់ពិនិត្យ។ បណ្ដាញដែលប្រើរួមគ្នាអាចធ្វើឱ្យសម្គាល់ខុសបាន។ នេះមិនបញ្ជាក់ថាគណនីត្រូវបានលួចទេ។',
  deviceHint: 'ព័ត៌មានកម្មវិធីរុករក និងឧបករណ៍គឺជាការប៉ាន់ស្មានតាម user-agent។',
} }
const enReasons: Record<string, string> = {
  authenticated: 'Signed in successfully', invalid_credentials: 'Incorrect username or password',
  account_locked: 'Account locked', account_disabled: 'Account deactivated', account_expired: 'Account expired', credentials_expired: 'Password expired',
  authentication_failed: 'Authentication failed', account_link_required: 'Sign in to the existing account and link this provider', access_denied: 'Provider denied access',
  social_login_failed: 'Social sign-in failed', invalid_state_parameter: 'Invalid sign-in state', authorization_request_not_found: 'Sign-in request expired or missing',
  invalid_id_token: 'Invalid provider ID token', invalid_token_response: 'Invalid provider token response', invalid_user_info_response: 'Invalid provider profile',
  invalid_grant: 'Provider rejected authorization', invalid_client: 'Provider client configuration error', invalid_request: 'Invalid provider request',
  server_error: 'Provider server error', temporarily_unavailable: 'Provider temporarily unavailable', profile_completion_required: 'Profile information required',
}
const kmReasons: Record<string, string> = {
  authenticated: 'បានចូលដោយជោគជ័យ', invalid_credentials: 'ឈ្មោះចូល ឬពាក្យសម្ងាត់មិនត្រឹមត្រូវ',
  account_locked: 'គណនីត្រូវបានចាក់សោ', account_disabled: 'គណនីត្រូវបានបិទ', account_expired: 'គណនីផុតកំណត់', credentials_expired: 'ពាក្យសម្ងាត់ផុតកំណត់',
  authentication_failed: 'ការផ្ទៀងផ្ទាត់បរាជ័យ', account_link_required: 'សូមចូលគណនីដែលមានស្រាប់ ហើយភ្ជាប់អ្នកផ្ដល់សេវានេះ', access_denied: 'អ្នកផ្ដល់សេវាបដិសេធការចូល',
  social_login_failed: 'ការចូលតាមសង្គមបរាជ័យ', invalid_state_parameter: 'ស្ថានភាពចូលមិនត្រឹមត្រូវ', authorization_request_not_found: 'សំណើចូលបានផុតកំណត់ ឬបាត់',
  invalid_id_token: 'ថូខឹនអត្តសញ្ញាណអ្នកផ្ដល់សេវាមិនត្រឹមត្រូវ', invalid_token_response: 'ការឆ្លើយតបថូខឹនមិនត្រឹមត្រូវ', invalid_user_info_response: 'ព័ត៌មានអ្នកផ្ដល់សេវាមិនត្រឹមត្រូវ',
  invalid_grant: 'អ្នកផ្ដល់សេវាបដិសេធការអនុញ្ញាត', invalid_client: 'កំហុសការកំណត់កម្មវិធីអ្នកផ្ដល់សេវា', invalid_request: 'សំណើទៅអ្នកផ្ដល់សេវាមិនត្រឹមត្រូវ',
  server_error: 'កំហុសម៉ាស៊ីនមេអ្នកផ្ដល់សេវា', temporarily_unavailable: 'អ្នកផ្ដល់សេវាមិនអាចប្រើបានបណ្ដោះអាសន្ន', profile_completion_required: 'ត្រូវការបំពេញព័ត៌មានគណនី',
}
export function historyReason(reason: string, locale: 'en' | 'km', successful: boolean) {
  return (locale === 'km' ? kmReasons : enReasons)[reason] || (successful ? historyMessages[locale].success : historyMessages[locale].failure)
}
