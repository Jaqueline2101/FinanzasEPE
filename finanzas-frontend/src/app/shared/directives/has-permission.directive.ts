import { Directive, Input, TemplateRef, ViewContainerRef, OnDestroy } from '@angular/core';
import { PermissionService2 } from '../../features/auth/services/permission2.service';
import { Subscription } from 'rxjs';

@Directive({
  selector: '[appHasPermission]',
  standalone: true
})
export class HasPermissionDirective implements OnDestroy {
  private subscription: Subscription;
  private currentPermissions = new Set<string>();
  private logicalOp: 'AND' | 'OR' = 'OR';
  private requiredPermissions: string[] = [];

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private permissionService: PermissionService2
  ) {
    this.subscription = this.permissionService.permisos$.subscribe(perms => {
      this.currentPermissions = perms;
      this.updateView();
    });
  }

  @Input() set appHasPermission(value: string | string[]) {
    this.requiredPermissions = Array.isArray(value) ? value : [value];
    this.updateView();
  }

  @Input() set appHasPermissionOp(op: 'AND' | 'OR') {
    this.logicalOp = op;
    this.updateView();
  }

  private updateView() {
    if (this.checkPermissions()) {
      if (this.viewContainer.length === 0) {
        this.viewContainer.createEmbeddedView(this.templateRef);
      }
    } else {
      this.viewContainer.clear();
    }
  }

  private checkPermissions(): boolean {
    if (this.permissionService.isAdmin() || this.currentPermissions.has('ACCESO_TOTAL')) return true;
    if (this.requiredPermissions.length === 0) return true;

    if (this.logicalOp === 'OR') {
      return this.requiredPermissions.some(p => this.currentPermissions.has(p));
    } else {
      return this.requiredPermissions.every(p => this.currentPermissions.has(p));
    }
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }
}
