import { Injectable } from '@angular/core';
import Swal, { SweetAlertResult } from 'sweetalert2';

@Injectable({ providedIn: 'root' })
export class AlertService {
  success(title: string, text?: string) {
    return Swal.fire({ icon: 'success', title, text, confirmButtonText: 'OK' });
  }

  error(title: string, text?: string) {
    // se text for um objeto, transforma em string legível
    const message =
      typeof text === 'object' ? JSON.stringify(text, null, 2) : text;
    return Swal.fire({
      icon: 'error',
      title,
      text: message,
      confirmButtonText: 'OK',
    });
  }

  info(title: string, text?: string) {
    return Swal.fire({ icon: 'info', title, text, confirmButtonText: 'OK' });
  }

  confirm(
    title: string,
    text?: string,
    confirmText = 'Sim',
    cancelText = 'Cancelar'
  ): Promise<boolean> {
    return Swal.fire({
      title,
      text,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: confirmText,
      cancelButtonText: cancelText,
      reverseButtons: true,
    }).then((result: SweetAlertResult) => !!result.isConfirmed);
  }

  // utilitário para mostrar carregando (opcional)
  loading(title = 'Aguarde...') {
    Swal.fire({
      title,
      allowOutsideClick: false,
      didOpen: () => Swal.showLoading(),
    });
  }

  close() {
    Swal.close();
  }
}
