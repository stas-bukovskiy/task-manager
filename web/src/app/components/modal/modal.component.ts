import {Component, Input} from '@angular/core';
import {TitleCasePipe} from "@angular/common";
import {ModalService} from "../../_services/modal.service";

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [
    TitleCasePipe
  ],
  templateUrl: './modal.component.html',
  styleUrl: './modal.component.scss'
})
export class ModalComponent {
  @Input() title: string

  constructor(public modalService: ModalService) {
  }
}
