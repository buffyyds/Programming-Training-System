/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { JudgeConfig } from './JudgeConfig';
import type { UserVO } from './UserVO';
export type QuestionVO = {
    acceptedPercent?: number;
    content?: string;
    createTime?: string;
    favourNum?: number;
    id?: number;
    isCompletion?: boolean;
    isWrongQuestion?: boolean;
    judgeCase?: string;
    judgeConfig?: JudgeConfig;
    studentNum?: number;
    submitNum?: number;
    tags?: Array<string>;
    thumbNum?: number;
    title?: string;
    updateTime?: string;
    userId?: number;
    userVO?: UserVO;
};

