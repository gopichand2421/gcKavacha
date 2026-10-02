import { authStorage } from "../auth/authStorage";

const API_BASE_URL = "http://localhost:8080";

export async function httpClient<T>(
    path: string,
    options: RequestInit = {}
): Promise<T> {

    const token = authStorage.getToken();

    const headers = new Headers(options.headers)

    headers.set("Content-Type", "application/json");

    if (token) {
        headers.set("Authorization", `Bearer ${token}`);
    }

    const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers }); 
    if (!response.ok) { 
        const errorBody = await response.text(); 
        throw new Error(errorBody || `Request failed: ${response.status}`); 
    } 
    if (response.status === 204) { 
        return undefined as T; 
    }
    
    return response.json() as Promise<T>;
}