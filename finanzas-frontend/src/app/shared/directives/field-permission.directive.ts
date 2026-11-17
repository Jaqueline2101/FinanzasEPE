import { Directive, Input, ElementRef, Renderer2, OnDestroy } from '@angular/core';
import { PermissionService2 } from '../../features/auth/services/permission2.service';
import { Subscription } from 'rxjs';

@Directive({
  selector: '[appFieldPermission]',
  standalone: true
})
export class FieldPermissionDirective implements OnDestroy {
  private subscription: Subscription;
  private currentPermissions = new Set<string>();
  private requiredPermissions: string[] = [];

  constructor(
    private el: ElementRef,
    private renderer: Renderer2,
    private permissionService: PermissionService2
  ) {
    this.subscription = this.permissionService.permisos$.subscribe(perms => {
      this.currentPermissions = perms;
      this.updateState();
    });
  }

  @Input() set appFieldPermission(value: string | string[]) {
    this.requiredPermissions = Array.isArray(value) ? value : [value];
    this.updateState();
  }

  private updateState() {
    const hasPermission = this.checkPermissions();
    if (!hasPermission) {
      this.renderer.setAttribute(this.el.nativeElement, 'disabled', 'true');
      this.renderer.addClass(this.el.nativeElement, 'permission-disabled');
      // Para componentes que no usan el atributo disabled nativo
      this.renderer.setStyle(this.el.nativeElement, 'pointer-events', 'none');
      this.renderer.setStyle(this.el.nativeElement, 'opacity', '0.6');
    } else {
      this.renderer.removeAttribute(this.el.nativeElement, 'disabled');
      this.renderer.removeClass(this.el.nativeElement, 'permission-disabled');
      this.renderer.removeStyle(this.el.nativeElement, 'pointer-events');
      this.renderer.removeStyle(this.el.nativeElement, 'opacity');
    }
  }

  private checkPermissions(): boolean {
    if (this.requiredPermissions.length === 0) return true;
    return this.requiredPermissions.some(p => this.currentPermissions.has(p));
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }
}
