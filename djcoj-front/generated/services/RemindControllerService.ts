/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_boolean_ } from "../models/BaseResponse_boolean_";
import type { BaseResponse_long_ } from "../models/BaseResponse_long_";
import type { BaseResponse_Page_RemindComplete_ } from "../models/BaseResponse_Page_RemindComplete_";
import type { RemindCompleteAddRequest } from "../models/RemindCompleteAddRequest";
import type { RemindCompleteQueryRequest } from "../models/RemindCompleteQueryRequest";
import type { CancelablePromise } from "../core/CancelablePromise";
import { OpenAPI } from "../core/OpenAPI";
import { request as __request } from "../core/request";

export class RemindControllerService {
  /**
   * addRemind
   * @param remindCompleteAddRequest remindCompleteAddRequest
   * @returns BaseResponse_boolean_ OK
   * @returns any Created
   * @throws ApiError
   */
  public static addRemindUsingPost(
    remindCompleteAddRequest: RemindCompleteAddRequest
  ): CancelablePromise<BaseResponse_boolean_ | any> {
    return __request(OpenAPI, {
      method: "POST",
      url: "/api/remind/add",
      body: remindCompleteAddRequest,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * getUnread
   * @returns BaseResponse_long_ OK
   * @throws ApiError
   */
  public static getUnreadUsingGet(): CancelablePromise<BaseResponse_long_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/remind/get/unread",
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * listRemindCompleteByPage
   * @param remindCompleteQueryRequest remindCompleteQueryRequest
   * @returns BaseResponse_Page_RemindComplete_ OK
   * @returns any Created
   * @throws ApiError
   */
  public static listRemindCompleteByPageUsingPost(
    remindCompleteQueryRequest: RemindCompleteQueryRequest
  ): CancelablePromise<BaseResponse_Page_RemindComplete_ | any> {
    return __request(OpenAPI, {
      method: "POST",
      url: "/api/remind/list/page",
      body: remindCompleteQueryRequest,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * markAsRead
   * @param id id
   * @returns BaseResponse_boolean_ OK
   * @returns any Created
   * @throws ApiError
   */
  public static markAsReadUsingPut(
    id: number
  ): CancelablePromise<BaseResponse_boolean_ | any> {
    return __request(OpenAPI, {
      method: "PUT",
      url: "/api/remind/markAsRead/{id}",
      path: {
        id: id,
      },
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }
}
