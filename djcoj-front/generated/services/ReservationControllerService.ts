/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_List_ReservationPerformanceVO_ } from "../models/BaseResponse_List_ReservationPerformanceVO_";
import type { BaseResponse_List_ReservationVO_ } from "../models/BaseResponse_List_ReservationVO_";
import type { BaseResponse_string_ } from "../models/BaseResponse_string_";
import type { DoReservationRequest } from "../models/DoReservationRequest";
import type { ReservationEditRequest } from "../models/ReservationEditRequest";
import type { CancelablePromise } from "../core/CancelablePromise";
import { OpenAPI } from "../core/OpenAPI";
import { request as __request } from "../core/request";

export class ReservationControllerService {
  /**
   * addReservation
   * @param timeSlot time_slot
   * @returns BaseResponse_string_ OK
   * @throws ApiError
   */
  public static addReservationUsingGet(
    timeSlot?: string
  ): CancelablePromise<BaseResponse_string_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/reservation/add/teacher",
      query: {
        time_slot: timeSlot,
      },
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * deleteReservation
   * @param reservationId reservationId
   * @returns BaseResponse_string_ OK
   * @throws ApiError
   */
  public static deleteReservationUsingGet(
    reservationId?: number
  ): CancelablePromise<BaseResponse_string_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/reservation/delete/teacher",
      query: {
        reservationId: reservationId,
      },
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * doReservation
   * @param doReservationRequest doReservationRequest
   * @returns BaseResponse_string_ OK
   * @returns any Created
   * @throws ApiError
   */
  public static doReservationUsingPost(
    doReservationRequest: DoReservationRequest
  ): CancelablePromise<BaseResponse_string_ | any> {
    return __request(OpenAPI, {
      method: "POST",
      url: "/api/reservation/doReservation",
      body: doReservationRequest,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * getReservation
   * @returns BaseResponse_List_ReservationVO_ OK
   * @throws ApiError
   */
  public static getReservationUsingGet(): CancelablePromise<BaseResponse_List_ReservationVO_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/reservation/get",
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * getTOPTenStudentReservationByCount
   * @returns BaseResponse_List_ReservationPerformanceVO_ OK
   * @throws ApiError
   */
  public static getTopTenStudentReservationByCountUsingGet(): CancelablePromise<BaseResponse_List_ReservationPerformanceVO_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/reservation/getTOPTenStudentReservationByCount",
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * getTOPTenStudentReservationByTotleTime
   * @returns BaseResponse_List_ReservationPerformanceVO_ OK
   * @throws ApiError
   */
  public static getTopTenStudentReservationByTotleTimeUsingGet(): CancelablePromise<BaseResponse_List_ReservationPerformanceVO_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/reservation/getTOPTenStudentReservationByTotleTime",
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * unDoReservation
   * @param doReservationRequest doReservationRequest
   * @returns BaseResponse_string_ OK
   * @returns any Created
   * @throws ApiError
   */
  public static unDoReservationUsingPost(
    doReservationRequest: DoReservationRequest
  ): CancelablePromise<BaseResponse_string_ | any> {
    return __request(OpenAPI, {
      method: "POST",
      url: "/api/reservation/unDoReservation",
      body: doReservationRequest,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * updateReservation
   * @param reservationEditRequest reservationEditRequest
   * @returns BaseResponse_string_ OK
   * @returns any Created
   * @throws ApiError
   */
  public static updateReservationUsingPost(
    reservationEditRequest: ReservationEditRequest
  ): CancelablePromise<BaseResponse_string_ | any> {
    return __request(OpenAPI, {
      method: "POST",
      url: "/api/reservation/update/teacher",
      body: reservationEditRequest,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }
}
