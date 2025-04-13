/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { UserVO } from './UserVO';
export type ReservationVO = {
    id?: number;
    isReservation?: boolean;
    studentUser?: UserVO;
    teacherUser?: UserVO;
    time_slot?: string;
};

