const TOEKN_KEY = "gckavacha_access_token";

export const authStorage = {
    getToken(): string | null {
        return sessionStorage.getItem(TOEKN_KEY);
    },

    setToken(token: string): void {
        sessionStorage.setItem(TOEKN_KEY, token);
    },

    clearToken(): void{
        sessionStorage.removeItem(TOEKN_KEY);
    },

    hasToken(): boolean{
        return sessionStorage.getItem(TOEKN_KEY) !== null;
    }
}