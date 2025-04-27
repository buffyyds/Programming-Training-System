/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_List_StudentsVo_ } from "../models/BaseResponse_List_StudentsVo_";
import type { BaseResponse_List_TeacherVo_ } from "../models/BaseResponse_List_TeacherVo_";
import type { BaseResponse_string_ } from "../models/BaseResponse_string_";
import type { BaseResponse_TeacherVo_ } from "../models/BaseResponse_TeacherVo_";
import type { CancelablePromise } from "../core/CancelablePromise";
import { OpenAPI } from "../core/OpenAPI";
import { request as __request } from "../core/request";

export class TasControllerService {
  /**
   * getStudentList
   * @returns BaseResponse_List_StudentsVo_ OK
   * @throws ApiError
   */
  public static getStudentListUsingGet(): CancelablePromise<BaseResponse_List_StudentsVo_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/TAS/getStudentList",
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

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

  /**
   * getTeacherList
   * @returns BaseResponse_List_TeacherVo_ OK
   * @throws ApiError
   */
  public static getTeacherListUsingGet(): CancelablePromise<BaseResponse_List_TeacherVo_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/TAS/getTeacherList",
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * kickStudent
   * @param studentId studentId
   * @returns BaseResponse_string_ OK
   * @throws ApiError
   */
  public static kickStudentUsingGet(
    studentId?: number
  ): CancelablePromise<BaseResponse_string_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/TAS/kickStudent",
      query: {
        studentId: studentId,
      },
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }
}
