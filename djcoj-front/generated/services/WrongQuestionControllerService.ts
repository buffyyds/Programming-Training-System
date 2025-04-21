/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_boolean_ } from "../models/BaseResponse_boolean_";
import type { BaseResponse_List_Wrongquestion_ } from "../models/BaseResponse_List_Wrongquestion_";
import type { BaseResponse_List_WrongquestionDetailVO_ } from "../models/BaseResponse_List_WrongquestionDetailVO_";
import type { BaseResponse_List_WrongquestionPerformanceVO_ } from "../models/BaseResponse_List_WrongquestionPerformanceVO_";
import type { BaseResponse_List_WrongquestionVO_ } from "../models/BaseResponse_List_WrongquestionVO_";
import type { CancelablePromise } from "../core/CancelablePromise";
import { OpenAPI } from "../core/OpenAPI";
import { request as __request } from "../core/request";

export class WrongQuestionControllerService {
  /**
   * doWrongQuestion
   * @param questionId questionId
   * @returns BaseResponse_boolean_ OK
   * @throws ApiError
   */
  public static doWrongQuestionUsingGet(
    questionId?: number
  ): CancelablePromise<BaseResponse_boolean_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/wrongQuestion/doWrongQuestion",
      query: {
        questionId: questionId,
      },
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * getWrongQuestionAnalysis
   * @returns BaseResponse_List_WrongquestionPerformanceVO_ OK
   * @throws ApiError
   */
  public static getWrongQuestionAnalysisUsingGet(): CancelablePromise<BaseResponse_List_WrongquestionPerformanceVO_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/wrongQuestion/getWrongQuestionAnalysis",
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * getWrongQuestionDetailByTeacher
   * @param questionId questionId
   * @returns BaseResponse_List_WrongquestionDetailVO_ OK
   * @throws ApiError
   */
  public static getWrongQuestionDetailByTeacherUsingGet(
    questionId?: number
  ): CancelablePromise<BaseResponse_List_WrongquestionDetailVO_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/wrongQuestion/getWrongQuestionDetailByTeacher",
      query: {
        questionId: questionId,
      },
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * getWrongQuestionListByStudent
   * @returns BaseResponse_List_Wrongquestion_ OK
   * @throws ApiError
   */
  public static getWrongQuestionListByStudentUsingGet(): CancelablePromise<BaseResponse_List_Wrongquestion_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/wrongQuestion/getWrongQuestionListByStudent",
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * getWrongQuestionListByTeacher
   * @returns BaseResponse_List_WrongquestionVO_ OK
   * @throws ApiError
   */
  public static getWrongQuestionListByTeacherUsingGet(): CancelablePromise<BaseResponse_List_WrongquestionVO_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/wrongQuestion/getWrongQuestionListByTeacher",
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }

  /**
   * unDoWrongQuestion
   * @param questionId questionId
   * @returns BaseResponse_boolean_ OK
   * @throws ApiError
   */
  public static unDoWrongQuestionUsingGet(
    questionId?: number
  ): CancelablePromise<BaseResponse_boolean_> {
    return __request(OpenAPI, {
      method: "GET",
      url: "/api/wrongQuestion/unDoWrongQuestion",
      query: {
        questionId: questionId,
      },
      errors: {
        401: `Unauthorized`,
        403: `Forbidden`,
        404: `Not Found`,
      },
    });
  }
}
