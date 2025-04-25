/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ReplyVO } from './ReplyVO';
import type { UserVO } from './UserVO';
export type PostVO = {
    content?: string;
    createTime?: string;
    hasThumb?: boolean;
    id?: number;
    isContainsSensitiveWord?: boolean;
    isReply?: boolean;
    questionId?: number;
    reply?: Array<ReplyVO>;
    thumbNum?: number;
    updateTime?: string;
    user?: UserVO;
    userId?: number;
};

