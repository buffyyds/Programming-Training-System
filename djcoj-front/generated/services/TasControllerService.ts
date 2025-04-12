/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_List_StudentsVo_ } from "../models/BaseResponse_List_StudentsVo_";
import type { BaseResponse_TeacherVo_ } from "../models/BaseResponse_TeacherVo_";
import type { CancelablePromise } from "../core/CancelablePromise";
import { OpenAPI } from "../core/OpenAPI";
import { request as __request } from "../core/request";

export class TasControllerService {
  /**
   * getStudents
   * @returns BaseResponse_List_StudentsVo_ OK
   * @throws ApiError
   */
  public static getStudentsUsingGet(): CancelablePromise<BaseResponse_List_StudentsVo_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/TAS/getStudents",
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * getTeacher
   * @returns BaseResponse_TeacherVo_ OK
   * @throws ApiError
   */
  public static getTeacherUsingGet(): CancelablePromise<BaseResponse_TeacherVo_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/TAS/getTeacher",
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }
}
