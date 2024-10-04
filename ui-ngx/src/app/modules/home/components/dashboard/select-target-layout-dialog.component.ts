///
/// Copyright © 2016-2024 The Sobeam Authors
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///     http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { AppState } from '@core/core.state';
import { Router } from '@angular/router';
import { DialogComponent } from '@app/shared/components/dialog.component';
import { DashboardLayoutId } from '@app/shared/models/dashboard.models';

@Component({
  selector: 'tb-select-target-layout-dialog',
  templateUrl: './select-target-layout-dialog.component.html',
  styleUrls: ['./layout-button.scss']
})
export class SelectTargetLayoutDialogComponent extends DialogComponent<SelectTargetLayoutDialogComponent, DashboardLayoutId>
  implements OnInit {

  constructor(protected store: Store<AppState>,
              protected router: Router,
              public dialogRef: MatDialogRef<SelectTargetLayoutDialogComponent, DashboardLayoutId>) {
    super(store, router, dialogRef);
  }

  ngOnInit(): void {
  }

  selectLayout(layoutId: DashboardLayoutId) {
    this.dialogRef.close(layoutId);
  }

  cancel(): void {
    this.dialogRef.close(null);
  }

}
