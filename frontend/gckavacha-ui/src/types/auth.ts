export interface LoginRequest{
    username: string,
    password: string
}

export interface LoginResponse {
    token: string,
    tokenType: string,
    expiresIn: number
}

export type Role = "USER" | "ADMIN";

export type UserStatus = "ACTIVE" | "DISABLED" | "LOCKED";

export interface CurrentUser{
    id: string,
    username: string,
    email: string,
    firstName: string,
    lastName: string,
    roles: Role[],
    status: UserStatus,
    createdAt: string
}

