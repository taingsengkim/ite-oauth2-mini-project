export type AccountLocale = 'en' | 'km'
const en = {
  title: 'My profile', description: 'Manage your personal information and account language.', loading: 'Loading your profile...', retry: 'Retry',
  username: 'Username', email: 'Email', givenName: 'Given name', familyName: 'Family name', nickname: 'Nickname', phoneNumber: 'Phone number',
  pictureUrl: 'Profile picture URL', locale: 'Language', save: 'Save profile', saving: 'Saving...', discard: 'Discard changes', reload: 'Reload saved profile',
  pictureHelp: 'Use an HTTPS image URL. Leave empty or choose Remove picture to clear it.', removePicture: 'Remove picture', pictureAlt: 'Profile picture',
  phoneHelp: 'Optional. Use international format, such as +85512345678.', verified: 'Verified', unverified: 'Not verified', resetVerification: 'Verification will be reset',
  identifiersWarning: 'Changing your username or email signs out all browser sessions and revokes saved app authorizations and consents. Sign in again with your updated details. Already-issued JWTs may remain valid until expiry.',
  saved: 'Your profile was saved successfully.', signedOut: 'Your profile was saved. Sign in again with your updated username or email.', signIn: 'Sign in again',
  created: 'Account created', updated: 'Last updated', loadFailed: 'Could not load your profile.', saveFailed: 'Could not save your profile. Please try again.',
  signInRequired: 'Your session has expired. Sign in again to continue.', forbidden: 'The security token expired or access was denied. Reload and try again.',
  conflict: 'Your profile changed in another session. Reload the saved profile before trying again.', usernameTaken: 'This username is already registered.',
  usernameReserved: 'This username is reserved by another account.', emailTaken: 'This email is already registered.', phoneTaken: 'This phone number is already registered.',
  required: 'This field is required.', tooLong: 'This value is too long.', usernameRule: 'Use 3–50 letters, digits, dots, underscores, or hyphens.',
  emailRule: 'Enter a valid email address.', phoneRule: 'Use international format, starting with +.', pictureRule: 'Use an HTTPS URL without credentials or fragments.',
  languageRule: 'Choose English or Khmer.', photoPreview: 'Picture preview', emailHelp: 'Changing this address resets email verification.',
}
const km: Record<keyof typeof en, string> = {
  title: 'ប្រវត្តិរូបរបស់ខ្ញុំ', description: 'គ្រប់គ្រងព័ត៌មានផ្ទាល់ខ្លួន និងភាសាគណនីរបស់អ្នក។', loading: 'កំពុងផ្ទុកប្រវត្តិរូប...', retry: 'ព្យាយាមម្ដងទៀត',
  username: 'ឈ្មោះអ្នកប្រើ', email: 'អ៊ីមែល', givenName: 'នាមខ្លួន', familyName: 'នាមត្រកូល', nickname: 'ឈ្មោះហៅក្រៅ', phoneNumber: 'លេខទូរស័ព្ទ',
  pictureUrl: 'តំណរូបភាពប្រវត្តិរូប', locale: 'ភាសា', save: 'រក្សាទុកប្រវត្តិរូប', saving: 'កំពុងរក្សាទុក...', discard: 'បោះបង់ការកែប្រែ', reload: 'ផ្ទុកព័ត៌មានដែលបានរក្សាទុកឡើងវិញ',
  pictureHelp: 'ប្រើតំណរូបភាព HTTPS។ ទុកទទេ ឬចុចលុបរូបភាពដើម្បីដករូបភាពចេញ។', removePicture: 'លុបរូបភាព', pictureAlt: 'រូបភាពប្រវត្តិរូប',
  phoneHelp: 'មិនចាំបាច់បំពេញ។ ប្រើទម្រង់អន្តរជាតិ ដូចជា +85512345678។', verified: 'បានផ្ទៀងផ្ទាត់', unverified: 'មិនទាន់ផ្ទៀងផ្ទាត់', resetVerification: 'ត្រូវផ្ទៀងផ្ទាត់ឡើងវិញ',
  identifiersWarning: 'ការប្ដូរឈ្មោះអ្នកប្រើ ឬអ៊ីមែលនឹងចាកចេញពីវគ្គទាំងអស់ និងដកការអនុញ្ញាតរបស់កម្មវិធីដែលបានរក្សាទុក។ សូមចូលម្ដងទៀតដោយប្រើព័ត៌មានថ្មី។ JWT ដែលបានចេញរួចអាចនៅមានសុពលភាពរហូតដល់ផុតកំណត់។',
  saved: 'ប្រវត្តិរូបរបស់អ្នកត្រូវបានរក្សាទុកដោយជោគជ័យ។', signedOut: 'ប្រវត្តិរូបត្រូវបានរក្សាទុក។ សូមចូលម្ដងទៀតដោយប្រើឈ្មោះអ្នកប្រើ ឬអ៊ីមែលថ្មី។', signIn: 'ចូលគណនីម្ដងទៀត',
  created: 'កាលបរិច្ឆេទបង្កើតគណនី', updated: 'កែប្រែចុងក្រោយ', loadFailed: 'មិនអាចផ្ទុកប្រវត្តិរូបបានទេ។', saveFailed: 'មិនអាចរក្សាទុកប្រវត្តិរូបបានទេ។ សូមព្យាយាមម្ដងទៀត។',
  signInRequired: 'វគ្គរបស់អ្នកបានផុតកំណត់។ សូមចូលម្ដងទៀត។', forbidden: 'សញ្ញាសុវត្ថិភាពបានផុតកំណត់ ឬគ្មានសិទ្ធិ។ សូមផ្ទុកឡើងវិញ ហើយព្យាយាមម្ដងទៀត។',
  conflict: 'ប្រវត្តិរូបត្រូវបានកែប្រែនៅវគ្គផ្សេង។ សូមផ្ទុកព័ត៌មានដែលបានរក្សាទុកឡើងវិញ មុនព្យាយាមម្ដងទៀត។', usernameTaken: 'ឈ្មោះអ្នកប្រើនេះត្រូវបានប្រើរួចហើយ។',
  usernameReserved: 'ឈ្មោះអ្នកប្រើនេះត្រូវបានរក្សាទុកសម្រាប់គណនីផ្សេង។', emailTaken: 'អ៊ីមែលនេះត្រូវបានប្រើរួចហើយ។', phoneTaken: 'លេខទូរស័ព្ទនេះត្រូវបានប្រើរួចហើយ។',
  required: 'សូមបំពេញព័ត៌មាននេះ។', tooLong: 'ព័ត៌មាននេះវែងពេក។', usernameRule: 'ប្រើអក្សរ លេខ ចំណុច សញ្ញាគូសក្រោម ឬសហសញ្ញា ចំនួន ៣–៥០ តួអក្សរ។',
  emailRule: 'សូមបញ្ចូលអ៊ីមែលត្រឹមត្រូវ។', phoneRule: 'ប្រើទម្រង់អន្តរជាតិ ចាប់ផ្ដើមដោយ +។', pictureRule: 'ប្រើតំណ HTTPS ដែលគ្មានព័ត៌មានសម្ងាត់ ឬ fragment។',
  languageRule: 'សូមជ្រើសរើសភាសាខ្មែរ ឬអង់គ្លេស។', photoPreview: 'មើលរូបភាពជាមុន', emailHelp: 'ការប្ដូរអាសយដ្ឋាននេះត្រូវការផ្ទៀងផ្ទាត់អ៊ីមែលឡើងវិញ។',
}
export const accountMessages = { en, km }
