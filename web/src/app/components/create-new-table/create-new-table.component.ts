import {Component} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {NgIf} from "@angular/common";
import {ModalService} from "../../_services/modal.service";

@Component({
  selector: 'app-create-new-table',
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    ReactiveFormsModule
  ],
  templateUrl: './create-new-table.component.html',
  styleUrl: './create-new-table.component.scss'
})
export class CreateNewTableComponent {
  constructor(public modalService: ModalService) {
  }

  createNewBoardForm = new FormGroup({
    title: new FormControl<string>('', []),
    inviteMember: new FormControl<string>('', []),
  })

  createBoard() {

  }

  // get title() {
  //   return this.createNewBoardForm.controls.title as FormControl;
  // }
  get inviteMember() {
    return this.createNewBoardForm.controls.inviteMember as FormControl;
  }
}
