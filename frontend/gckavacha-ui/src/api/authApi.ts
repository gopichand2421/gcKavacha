import { httpClient } from './httpClient';

import { CurrentUser, LoginRequest, LoginResponse } from '../types/auth';


export async function login(request: LoginRequest): Promise<LoginResponse>{
    return httpClient<LoginResponse>("/api/auth/login",{
        method: "POST",
        body: JSON.stringify(request)
    });
}

export async function getCurrentUser(): Promise<CurrentUser>{
    return await httpClient<CurrentUser>(
        "/api/auth/me",
        {
            method: "GET"
        }
    );
}
