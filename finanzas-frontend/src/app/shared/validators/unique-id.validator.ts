import { AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { map, catchError, debounceTime, switchMap, first } from 'rxjs/operators';

/**
 * Validador asíncrono genérico para verificar si un ID ya existe
 * @param checkFn Función que retorna un Observable<boolean> indicando si el ID existe
 * @param currentId ID actual (para modo edición, no validar contra sí mismo)
 */
export function uniqueIdValidator(
    checkFn: (id: string) => Observable<boolean>,
    currentId?: string
): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
        if (!control.value) {
            return of(null);
        }

        // Si estamos editando y el ID es el mismo, no validar
        if (currentId && control.value === currentId) {
            return of(null);
        }

        return of(control.value).pipe(
            debounceTime(500), // Esperar 500ms después de que el usuario deje de escribir
            switchMap(id => checkFn(id).pipe(
                map(exists => exists ? { idDuplicado: true } : null),
                catchError(() => of(null)) // En caso de error, no bloquear
            )),
            first()
        );
    };
}
