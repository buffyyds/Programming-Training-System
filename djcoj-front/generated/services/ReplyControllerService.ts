/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_List_ReplyVO_ } from "../models/BaseResponse_List_ReplyVO_";
import type { BaseResponse_boolean_ } from "../models/BaseResponse_boolean_";
import type { CancelablePromise } from "../core/CancelablePromise";
import { OpenAPI } from "../core/OpenAPI";
import { request as __request } from "../core/request";

export class ReplyControllerService {
  /**
   * getMyReply
   * @returns BaseResponse_List_ReplyVO_ OK
   * @throws ApiError
   */
  public static getMyReplyUsingGet(): CancelablePromise<BaseResponse_List_ReplyVO_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/reply/getMyReply",
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * markAsRead
   * @param id 回复ID
   * @returns BaseResponse_boolean_ OK
   * @throws ApiError
   */
  public static markAsReadUsingPut(id: number): CancelablePromise<BaseResponse_boolean_> {
    return __request(OpenAPI, {
      method: "PUT",
      url: `/api/reply/markAsRead/${id}`,
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }
}
