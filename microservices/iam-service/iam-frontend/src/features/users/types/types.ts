export type User = {
  uuid: string
  username: string
  email: string | null
  emailVerified: boolean
  name: string | null
  locale: string | null
  pictureUrl: string | null
  enabled: boolean
  accountNonLocked: boolean
  roles: {
    uuid: string
    name: string
  }[]
  createdAt: string
  updatedAt: string | null
}

export type UserPageResponse = {
  content: User[]
  page: {
    number: number
    size: number
    totalElements: number
    totalPages: number
  }
}

export type UserDetails = User & {
  givenName: string
  familyName: string
  nickname: string | null
  phoneNumber: string | null
}

export type UpdateUserInput = {
  givenName: string
  familyName: string
  nickname: string
  phoneNumber: string
  locale: 'en' | 'km'
}

export type UserStatusAction = 'enable' | 'disable' | 'lock' | 'unlock'

export type CreateUserInput = {
  username: string
  email: string
  password: string
  givenName: string
  familyName: string
  locale: 'en' | 'km'
  phoneNumber: string | null
}
