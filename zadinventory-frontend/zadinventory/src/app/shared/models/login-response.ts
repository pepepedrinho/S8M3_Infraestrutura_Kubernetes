import { Usuario } from "./usuario";

export interface LoginResponse {
  token: string;
  email: string;
  tipoUsuario: string;
}
