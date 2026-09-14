CREATE TABLE "p_ai_chat_message" (
  "id" bigint NOT NULL,
  "conversation_id" character varying(64) NOT NULL,
  "message_type" character varying(8) NOT NULL,
  "content" text,
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" integer NOT NULL DEFAULT 0,
  "tenant_id" bigint,
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_ai_chat_message" IS 'AI对话消息表';
COMMENT ON COLUMN "p_ai_chat_message"."id" IS '主键ID';
COMMENT ON COLUMN "p_ai_chat_message"."conversation_id" IS '对话ID';
COMMENT ON COLUMN "p_ai_chat_message"."message_type" IS '消息类型';
COMMENT ON COLUMN "p_ai_chat_message"."content" IS '消息内容';
COMMENT ON COLUMN "p_ai_chat_message"."create_id" IS '创建人ID';
COMMENT ON COLUMN "p_ai_chat_message"."create_time" IS '创建时间';
COMMENT ON COLUMN "p_ai_chat_message"."update_id" IS '修改人ID';
COMMENT ON COLUMN "p_ai_chat_message"."update_time" IS '修改时间';
COMMENT ON COLUMN "p_ai_chat_message"."delete_flag" IS '删除标识';
COMMENT ON COLUMN "p_ai_chat_message"."tenant_id" IS '租户ID';
CREATE INDEX "idx_ai_chat_message_cid" ON "p_ai_chat_message" USING btree ("conversation_id");

CREATE TABLE "p_ai_conversation" (
  "id" bigint NOT NULL,
  "user_id" bigint NOT NULL,
  "conversation_id" character varying(64) NOT NULL,
  "conversation_name" character varying(255),
  "turn_count" integer NOT NULL DEFAULT 0,
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" integer NOT NULL DEFAULT 0,
  "tenant_id" bigint,
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_ai_conversation" IS 'AI对话表';
COMMENT ON COLUMN "p_ai_conversation"."id" IS '主键ID';
COMMENT ON COLUMN "p_ai_conversation"."user_id" IS '用户ID';
COMMENT ON COLUMN "p_ai_conversation"."conversation_id" IS '对话ID';
COMMENT ON COLUMN "p_ai_conversation"."conversation_name" IS '对话名称';
COMMENT ON COLUMN "p_ai_conversation"."turn_count" IS '对话轮次';
COMMENT ON COLUMN "p_ai_conversation"."create_id" IS '创建人ID';
COMMENT ON COLUMN "p_ai_conversation"."create_time" IS '创建时间';
COMMENT ON COLUMN "p_ai_conversation"."update_id" IS '修改人ID';
COMMENT ON COLUMN "p_ai_conversation"."update_time" IS '修改时间';
COMMENT ON COLUMN "p_ai_conversation"."delete_flag" IS '删除标识';
COMMENT ON COLUMN "p_ai_conversation"."tenant_id" IS '租户ID';
CREATE UNIQUE INDEX "uk_ai_conversation_cid" ON "p_ai_conversation" USING btree ("conversation_id");

CREATE TABLE "p_ai_knowledge_base" (
  "id" bigint NOT NULL,
  "kb_name" character varying(100) NOT NULL,
  "kb_desc" character varying(500),
  "cover_oss_file_id" bigint,
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" smallint DEFAULT 0,
  "tenant_id" bigint,
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_ai_knowledge_base" IS '知识库表';
COMMENT ON COLUMN "p_ai_knowledge_base"."id" IS '主键（雪花ID）';
COMMENT ON COLUMN "p_ai_knowledge_base"."kb_name" IS '知识库名称';
COMMENT ON COLUMN "p_ai_knowledge_base"."kb_desc" IS '知识库描述';
COMMENT ON COLUMN "p_ai_knowledge_base"."cover_oss_file_id" IS '封面图ID';
COMMENT ON COLUMN "p_ai_knowledge_base"."create_id" IS '创建人';
COMMENT ON COLUMN "p_ai_knowledge_base"."create_time" IS '创建时间';
COMMENT ON COLUMN "p_ai_knowledge_base"."update_id" IS '修改人';
COMMENT ON COLUMN "p_ai_knowledge_base"."update_time" IS '修改时间';
COMMENT ON COLUMN "p_ai_knowledge_base"."delete_flag" IS '删除标识（0-未删除）';
COMMENT ON COLUMN "p_ai_knowledge_base"."tenant_id" IS '租户ID';

CREATE TABLE "p_ai_knowledge_doc" (
  "id" bigint NOT NULL,
  "doc_name" character varying(255) NOT NULL,
  "doc_type" character varying(100),
  "oss_file_id" bigint NOT NULL,
  "parse_status" character varying(20) NOT NULL DEFAULT 'pending'::character varying,
  "vector_status" character varying(20) NOT NULL DEFAULT 'pending'::character varying,
  "upload_time" timestamp without time zone,
  "upload_id" bigint,
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" smallint NOT NULL DEFAULT 0,
  "tenant_id" bigint,
  "kb_id" bigint,
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_ai_knowledge_doc" IS '知识库文档表';
COMMENT ON COLUMN "p_ai_knowledge_doc"."id" IS '主键';
COMMENT ON COLUMN "p_ai_knowledge_doc"."doc_name" IS '文档名称';
COMMENT ON COLUMN "p_ai_knowledge_doc"."doc_type" IS '文档类型';
COMMENT ON COLUMN "p_ai_knowledge_doc"."oss_file_id" IS 'OSS文件表ID';
COMMENT ON COLUMN "p_ai_knowledge_doc"."parse_status" IS '内容解析状态：pending-待处理 processing-处理中 complete-完成 error-失败';
COMMENT ON COLUMN "p_ai_knowledge_doc"."vector_status" IS '向量处理状态：pending-待处理 processing-处理中 complete-完成 error-失败';
COMMENT ON COLUMN "p_ai_knowledge_doc"."upload_time" IS '上传时间';
COMMENT ON COLUMN "p_ai_knowledge_doc"."upload_id" IS '上传人（用户ID）';
COMMENT ON COLUMN "p_ai_knowledge_doc"."kb_id" IS '所属知识库ID（关联 p_ai_knowledge_base.id）';
CREATE INDEX "idx_ai_knowledge_doc_oss_file" ON "p_ai_knowledge_doc" USING btree ("oss_file_id");
CREATE INDEX "idx_p_ai_knowledge_doc_kb_id" ON "p_ai_knowledge_doc" USING btree ("kb_id");

CREATE TABLE "p_ai_knowledge_doc_content" (
  "id" bigint NOT NULL,
  "doc_id" bigint NOT NULL,
  "content" text,
  "parse_start_time" timestamp without time zone,
  "parse_end_time" timestamp without time zone,
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" smallint NOT NULL DEFAULT 0,
  "tenant_id" bigint,
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_ai_knowledge_doc_content" IS '文档内容表';
COMMENT ON COLUMN "p_ai_knowledge_doc_content"."id" IS '主键';
COMMENT ON COLUMN "p_ai_knowledge_doc_content"."doc_id" IS '关联文档表ID';
COMMENT ON COLUMN "p_ai_knowledge_doc_content"."content" IS '文档内容解析后的大文本';
COMMENT ON COLUMN "p_ai_knowledge_doc_content"."parse_start_time" IS '内容解析开始时间';
COMMENT ON COLUMN "p_ai_knowledge_doc_content"."parse_end_time" IS '内容解析结束时间';
CREATE INDEX "idx_ai_knowledge_doc_content_doc" ON "p_ai_knowledge_doc_content" USING btree ("doc_id");

CREATE TABLE "p_sys_api_log" (
  "id" bigint NOT NULL,
  "user_id" bigint,
  "user_name" character varying(64),
  "nick_name" character varying(64),
  "request_url" character varying(512),
  "request_method" character varying(16),
  "request_time" timestamp without time zone,
  "response_code" integer,
  "request_params" text,
  "response_result" text,
  "cost_time" bigint,
  "ip" character varying(64),
  "description" character varying(256),
  "tenant_id" bigint,
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" smallint,
  "operation_type" character varying(255),
  "module" character varying(255),
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_sys_api_log" IS '接口日志表';
COMMENT ON COLUMN "p_sys_api_log"."id" IS '主键';
COMMENT ON COLUMN "p_sys_api_log"."user_id" IS '请求用户ID';
COMMENT ON COLUMN "p_sys_api_log"."user_name" IS '请求用户名';
COMMENT ON COLUMN "p_sys_api_log"."nick_name" IS '请求用户昵称';
COMMENT ON COLUMN "p_sys_api_log"."request_url" IS '请求URL';
COMMENT ON COLUMN "p_sys_api_log"."request_method" IS '请求方法';
COMMENT ON COLUMN "p_sys_api_log"."request_time" IS '请求时间';
COMMENT ON COLUMN "p_sys_api_log"."response_code" IS '响应码';
COMMENT ON COLUMN "p_sys_api_log"."request_params" IS '请求参数';
COMMENT ON COLUMN "p_sys_api_log"."response_result" IS '返回值';
COMMENT ON COLUMN "p_sys_api_log"."cost_time" IS '耗时(ms)';
COMMENT ON COLUMN "p_sys_api_log"."ip" IS '请求IP';
COMMENT ON COLUMN "p_sys_api_log"."description" IS '操作描述';
COMMENT ON COLUMN "p_sys_api_log"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "p_sys_api_log"."create_id" IS '创建人';
COMMENT ON COLUMN "p_sys_api_log"."create_time" IS '创建时间';
COMMENT ON COLUMN "p_sys_api_log"."update_id" IS '修改人';
COMMENT ON COLUMN "p_sys_api_log"."update_time" IS '修改时间';
COMMENT ON COLUMN "p_sys_api_log"."delete_flag" IS '删除标识';
COMMENT ON COLUMN "p_sys_api_log"."operation_type" IS '操作类型';
COMMENT ON COLUMN "p_sys_api_log"."module" IS '功能模块';
CREATE INDEX "idx_tenant_api_log" ON "p_sys_api_log" USING btree ("tenant_id");

INSERT INTO "p_sys_api_log" ("id", "user_id", "user_name", "nick_name", "request_url", "request_method", "request_time", "response_code", "request_params", "response_result", "cost_time", "ip", "description", "tenant_id", "create_id", "create_time", "update_id", "update_time", "delete_flag", "operation_type", "module") VALUES ('455595449347039232', '373320178493861888', 'superadmin', '超级管理员', '/api/user/edit', 'PUT', '2026-09-10 17:54:39.476275', 200, '{"vo": {"id":900002,"enableFlag":1,"userName":"admin","nickName":"默认租户管理员","password":"******","deptId":372139251365388288,"userStatus":"0","userEmail":null,"userGender":"1","userPhone":null,"userAddress":null,"userDescription":null,"userType":"admin","userTag":null,"roleIds":[374729672067207168],"createTime":"2026-09-04 13:07:55"}}', '{"code":200,"msg":"操作成功！","data":true}', 200, '127.0.0.1', '编辑用户', 1, '373320178493861888', '2026-09-10 17:54:39.679714', NULL, NULL, 0, 'UPDATE', '用户管理'), ('455595685821898752', '373320178493861888', 'superadmin', '超级管理员', '/api/user/edit', 'PUT', '2026-09-10 17:55:35.928067', 200, '{"vo": {"id":900002,"enableFlag":1,"userName":"admin","nickName":"默认租户管理员","password":"******","deptId":372139251365388288,"userStatus":"0","userEmail":"6***@qq.com","userGender":"1","userPhone":"155****8888","userAddress":"XX省XX市XXX街道","userDescription":null,"userType":"admin","userTag":null,"roleIds":[374729672067207168],"createTime":"2026-09-04 13:07:55"}}', '{"code":200,"msg":"操作成功！","data":true}', 129, '127.0.0.1', '编辑用户', 1, '373320178493861888', '2026-09-10 17:55:36.058522', NULL, NULL, 0, 'UPDATE', '用户管理'), ('455596024306425856', '373320178493861888', 'superadmin', '超级管理员', '/api/tenantPackage/edit', 'PUT', '2026-09-10 17:56:56.711829', 200, '{"vo": {"id":1,"packageName":"默认全量套餐","permissionSigns":[],"enableFlag":1,"remark":"系统预置套餐，不选默认视为全选","menuCount":null,"createTime":null}}', '{"code":200,"msg":"操作成功！","data":true}', 48, '127.0.0.1', '编辑租户套餐', 1, '373320178493861888', '2026-09-10 17:56:56.759534', NULL, NULL, 0, 'UPDATE', '租户套餐管理');

CREATE TABLE "p_sys_dept" (
  "id" bigint NOT NULL,
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" smallint,
  "tenant_id" bigint,
  "dept_name" character varying(500),
  "order_num" integer,
  "charge_person" character varying(255),
  "charge_person_tel" character varying(255),
  "charge_person_email" character varying(255),
  "enable_flag" smallint,
  "parent_id" bigint,
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_sys_dept" IS '部门表';
COMMENT ON COLUMN "p_sys_dept"."id" IS '主键id';
COMMENT ON COLUMN "p_sys_dept"."create_id" IS '创建人';
COMMENT ON COLUMN "p_sys_dept"."create_time" IS '创建时间';
COMMENT ON COLUMN "p_sys_dept"."update_id" IS '修改人';
COMMENT ON COLUMN "p_sys_dept"."update_time" IS '修改时间';
COMMENT ON COLUMN "p_sys_dept"."delete_flag" IS '删除标识';
COMMENT ON COLUMN "p_sys_dept"."tenant_id" IS '租户id';
COMMENT ON COLUMN "p_sys_dept"."dept_name" IS '部门名称';
COMMENT ON COLUMN "p_sys_dept"."order_num" IS '排序号';
COMMENT ON COLUMN "p_sys_dept"."charge_person" IS '负责人';
COMMENT ON COLUMN "p_sys_dept"."charge_person_tel" IS '电话';
COMMENT ON COLUMN "p_sys_dept"."charge_person_email" IS '邮箱';
COMMENT ON COLUMN "p_sys_dept"."enable_flag" IS '是否启用';
COMMENT ON COLUMN "p_sys_dept"."parent_id" IS '上级部门ID';
CREATE INDEX "idx_tenant_dept" ON "p_sys_dept" USING btree ("tenant_id");

INSERT INTO "p_sys_dept" ("id", "create_id", "create_time", "update_id", "update_time", "delete_flag", "tenant_id", "dept_name", "order_num", "charge_person", "charge_person_tel", "charge_person_email", "enable_flag", "parent_id") VALUES ('372139251365388288', 1, '2026-01-23 10:49:32', '373320178493861888', '2026-01-27 16:24:53', 0, 1, 'XXX有限公司', 1, 'Luminous.X', '18888888888', '690278565@qq.com', 1, -1);

CREATE TABLE "p_sys_dept_permission" (
  "id" bigint NOT NULL,
  "dept_id" bigint,
  "permission_sign" character varying(500),
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_sys_dept_permission" IS '部门权限表';
COMMENT ON COLUMN "p_sys_dept_permission"."id" IS '主键ID';
COMMENT ON COLUMN "p_sys_dept_permission"."dept_id" IS '组织ID';
COMMENT ON COLUMN "p_sys_dept_permission"."permission_sign" IS '权限标识';
CREATE INDEX "p_sys_dept_permission_dept_id_IDX" ON "p_sys_dept_permission" USING btree ("dept_id");

CREATE TABLE "p_sys_dict" (
  "id" bigint NOT NULL,
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" smallint,
  "tenant_id" bigint,
  "dict_name" character varying(500),
  "dict_code" character varying(255),
  "dict_type" character varying(255),
  "enable_flag" smallint,
  "remark" character varying(2000),
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_sys_dict" IS '数据字典表';
COMMENT ON COLUMN "p_sys_dict"."id" IS '主键id';
COMMENT ON COLUMN "p_sys_dict"."create_id" IS '创建人id';
COMMENT ON COLUMN "p_sys_dict"."create_time" IS '创建时间';
COMMENT ON COLUMN "p_sys_dict"."update_id" IS '修改人id';
COMMENT ON COLUMN "p_sys_dict"."update_time" IS '修改时间';
COMMENT ON COLUMN "p_sys_dict"."delete_flag" IS '删除标识';
COMMENT ON COLUMN "p_sys_dict"."tenant_id" IS '租户id';
COMMENT ON COLUMN "p_sys_dict"."dict_name" IS '字典名称';
COMMENT ON COLUMN "p_sys_dict"."dict_code" IS '字典编码';
COMMENT ON COLUMN "p_sys_dict"."dict_type" IS '字典类型';
COMMENT ON COLUMN "p_sys_dict"."enable_flag" IS '是否启用';
COMMENT ON COLUMN "p_sys_dict"."remark" IS '备注';

INSERT INTO "p_sys_dict" ("id", "create_id", "create_time", "update_id", "update_time", "delete_flag", "tenant_id", "dict_name", "dict_code", "dict_type", "enable_flag", "remark") VALUES ('371928102215421952', 1, '2026-01-22 20:50:30', NULL, NULL, 0, 1, '系统-是否', 'system_yes_no', 'system', 1, '是-否：true-false'), ('371928550305501184', 1, '2026-01-22 20:52:16', NULL, NULL, 0, 1, '系统-启用标识', 'system_enable_flag', 'system', 1, '启用-禁用：true-false'), ('371929245259730944', 1, '2026-01-22 20:55:02', 1, '2026-01-22 20:55:15', 0, 1, '系统-日志类型', 'system_log_type', 'system', 1, '日志记录类型'), ('373315093105811456', 1, '2026-01-26 16:41:54', NULL, NULL, 0, 1, '系统-男女', 'system_gender', 'system', 1, '男-女：1-0'), ('374305046493261824', '373320178493861888', '2026-01-29 10:15:37', NULL, NULL, 0, 1, '系统-规则值类型', 'system_rule_value_type', 'system', 1, '规则管理-规则值类型字典');

CREATE TABLE "p_sys_dict_value" (
  "id" bigint NOT NULL,
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" smallint,
  "dict_id" bigint,
  "dict_label" character varying(500),
  "dict_value" character varying(255),
  "show_style" character varying(255),
  "order_num" integer,
  "tenant_id" bigint,
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_sys_dict_value" IS '数据字典项表';
COMMENT ON COLUMN "p_sys_dict_value"."id" IS '主键id';
COMMENT ON COLUMN "p_sys_dict_value"."create_id" IS '创建人id';
COMMENT ON COLUMN "p_sys_dict_value"."create_time" IS '创建时间';
COMMENT ON COLUMN "p_sys_dict_value"."update_id" IS '修改人id';
COMMENT ON COLUMN "p_sys_dict_value"."update_time" IS '修改时间';
COMMENT ON COLUMN "p_sys_dict_value"."delete_flag" IS '删除标识';
COMMENT ON COLUMN "p_sys_dict_value"."dict_id" IS '字典id';
COMMENT ON COLUMN "p_sys_dict_value"."dict_label" IS '字典项label';
COMMENT ON COLUMN "p_sys_dict_value"."dict_value" IS '字典项value';
COMMENT ON COLUMN "p_sys_dict_value"."show_style" IS '显示样式';
COMMENT ON COLUMN "p_sys_dict_value"."order_num" IS '排序号';
COMMENT ON COLUMN "p_sys_dict_value"."tenant_id" IS '租户id';

INSERT INTO "p_sys_dict_value" ("id", "create_id", "create_time", "update_id", "update_time", "delete_flag", "dict_id", "dict_label", "dict_value", "show_style", "order_num", "tenant_id") VALUES ('371928325432086528', 1, '2026-01-22 20:51:23', NULL, NULL, 0, '371928102215421952', '是', 'true', 'success', 1, 1), ('371928374530609152', 1, '2026-01-22 20:51:35', NULL, NULL, 0, '371928102215421952', '否', 'false', 'danger', 2, 1), ('371928604038729728', 1, '2026-01-22 20:52:29', NULL, NULL, 0, '371928550305501184', '启用', 'true', 'success', 1, 1), ('371928658514350080', 1, '2026-01-22 20:52:42', NULL, NULL, 0, '371928550305501184', '禁用', 'false', 'danger', 2, 1), ('371929437061058560', 1, '2026-01-22 20:55:48', NULL, NULL, 0, '371929245259730944', '登录日志', '1', 'info', 1, 1), ('371929480384024576', 1, '2026-01-22 20:55:58', NULL, NULL, 0, '371929245259730944', '接口日志', '2', 'info', 2, 1), ('373315161330360320', 1, '2026-01-26 16:42:10', NULL, NULL, 0, '373315093105811456', '男', '1', 'info', 1, 1), ('373315191382548480', 1, '2026-01-26 16:42:17', NULL, NULL, 0, '373315093105811456', '女', '0', 'info', 2, 1), ('374305388337426432', '373320178493861888', '2026-01-29 10:16:59', NULL, NULL, 0, '374305046493261824', '文本', 'text', 'info', 1, 1), ('374305480628891648', '373320178493861888', '2026-01-29 10:17:21', NULL, NULL, 0, '374305046493261824', '布尔', 'boolean', 'info', 2, 1), ('374305575629877248', '373320178493861888', '2026-01-29 10:17:43', NULL, NULL, 0, '374305046493261824', 'JSON', 'json', 'info', 3, 1), ('374305628863983616', '373320178493861888', '2026-01-29 10:17:56', NULL, NULL, 0, '374305046493261824', '数值', 'number', 'info', 4, 1);

CREATE TABLE "p_sys_login_log" (
  "id" bigint NOT NULL,
  "user_name" character varying(64),
  "nick_name" character varying(64),
  "login_ip" character varying(64),
  "login_time" timestamp without time zone,
  "browser" character varying(64),
  "os" character varying(64),
  "status" smallint,
  "message" character varying(256),
  "tenant_id" bigint,
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" smallint,
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_sys_login_log" IS '登录日志表';
COMMENT ON COLUMN "p_sys_login_log"."id" IS '主键';
COMMENT ON COLUMN "p_sys_login_log"."user_name" IS '用户名';
COMMENT ON COLUMN "p_sys_login_log"."nick_name" IS '用户昵称';
COMMENT ON COLUMN "p_sys_login_log"."login_ip" IS '登录IP';
COMMENT ON COLUMN "p_sys_login_log"."login_time" IS '登录时间';
COMMENT ON COLUMN "p_sys_login_log"."browser" IS '浏览器';
COMMENT ON COLUMN "p_sys_login_log"."os" IS '操作系统';
COMMENT ON COLUMN "p_sys_login_log"."status" IS '登录状态(1成功 0失败)';
COMMENT ON COLUMN "p_sys_login_log"."message" IS '提示信息';
COMMENT ON COLUMN "p_sys_login_log"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "p_sys_login_log"."create_id" IS '创建人';
COMMENT ON COLUMN "p_sys_login_log"."create_time" IS '创建时间';
COMMENT ON COLUMN "p_sys_login_log"."update_id" IS '修改人';
COMMENT ON COLUMN "p_sys_login_log"."update_time" IS '修改时间';
COMMENT ON COLUMN "p_sys_login_log"."delete_flag" IS '删除标识';
CREATE INDEX "idx_tenant_login_log" ON "p_sys_login_log" USING btree ("tenant_id");

INSERT INTO "p_sys_login_log" ("id", "user_name", "nick_name", "login_ip", "login_time", "browser", "os", "status", "message", "tenant_id", "create_id", "create_time", "update_id", "update_time", "delete_flag") VALUES ('455596121991766016', 'superadmin', '超级管理员', '127.0.0.1', '2026-09-10 17:57:20.047721', 'Edge', 'Windows', 1, '登录成功', 1, NULL, '2026-09-10 17:57:20.050680', NULL, NULL, 0), ('455596185921347584', 'admin', '默认租户管理员', '127.0.0.1', '2026-09-10 17:57:35.291763', 'Edge', 'Windows', 1, '登录成功', 1, NULL, '2026-09-10 17:57:35.291763', NULL, NULL, 0);

CREATE TABLE "p_sys_menu" (
  "id" bigint NOT NULL,
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" smallint,
  "tenant_id" bigint,
  "enable_flag" smallint,
  "menu_type" character varying(255),
  "menu_name" character varying(255),
  "route_path" character varying(255),
  "permission_sign" character varying(255),
  "component_path" character varying(255),
  "menu_icon" character varying(100),
  "order_num" integer,
  "external_link" character varying(500),
  "activation_path" character varying(255),
  "keep_alive" smallint,
  "hide_flag" smallint,
  "iframe_flag" smallint,
  "show_badge" smallint,
  "fixed_tab" smallint,
  "hide_tab" smallint,
  "full_screen" smallint,
  "parent_id" bigint,
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_sys_menu" IS '菜单表';
COMMENT ON COLUMN "p_sys_menu"."id" IS '主键id';
COMMENT ON COLUMN "p_sys_menu"."create_id" IS '创建人id';
COMMENT ON COLUMN "p_sys_menu"."create_time" IS '创建时间';
COMMENT ON COLUMN "p_sys_menu"."update_id" IS '更新者id';
COMMENT ON COLUMN "p_sys_menu"."update_time" IS '更新时间';
COMMENT ON COLUMN "p_sys_menu"."delete_flag" IS '删除标识';
COMMENT ON COLUMN "p_sys_menu"."tenant_id" IS '租户id';
COMMENT ON COLUMN "p_sys_menu"."enable_flag" IS '是否启用';
COMMENT ON COLUMN "p_sys_menu"."menu_type" IS '菜单类型';
COMMENT ON COLUMN "p_sys_menu"."menu_name" IS '菜单名称';
COMMENT ON COLUMN "p_sys_menu"."route_path" IS '路径地址';
COMMENT ON COLUMN "p_sys_menu"."permission_sign" IS '权限标识';
COMMENT ON COLUMN "p_sys_menu"."component_path" IS '组件路径';
COMMENT ON COLUMN "p_sys_menu"."menu_icon" IS '菜单图标';
COMMENT ON COLUMN "p_sys_menu"."order_num" IS '菜单排序';
COMMENT ON COLUMN "p_sys_menu"."external_link" IS '外部链接';
COMMENT ON COLUMN "p_sys_menu"."activation_path" IS '激活路径';
COMMENT ON COLUMN "p_sys_menu"."keep_alive" IS '页面缓存';
COMMENT ON COLUMN "p_sys_menu"."hide_flag" IS '隐藏菜单';
COMMENT ON COLUMN "p_sys_menu"."iframe_flag" IS '是否内嵌';
COMMENT ON COLUMN "p_sys_menu"."show_badge" IS '是否显示徽章';
COMMENT ON COLUMN "p_sys_menu"."fixed_tab" IS '固定标签';
COMMENT ON COLUMN "p_sys_menu"."hide_tab" IS '标签隐藏';
COMMENT ON COLUMN "p_sys_menu"."full_screen" IS '全屏页面';
COMMENT ON COLUMN "p_sys_menu"."parent_id" IS '父级ID';

INSERT INTO "p_sys_menu" ("id", "create_id", "create_time", "update_id", "update_time", "delete_flag", "tenant_id", "enable_flag", "menu_type", "menu_name", "route_path", "permission_sign", "component_path", "menu_icon", "order_num", "external_link", "activation_path", "keep_alive", "hide_flag", "iframe_flag", "show_badge", "fixed_tab", "hide_tab", "full_screen", "parent_id") VALUES ('321200533090585415', NULL, '2026-06-05 08:16:14', '373320178493861888', '2026-06-05 17:13:47', 0, 1, 1, 'menu', '日志管理', 'log', 'log:list', NULL, 'ri:file-list-line', 7, NULL, NULL, 0, 0, 0, NULL, NULL, NULL, NULL, '371233208555634688'), ('321200533295976747', NULL, '2026-06-05 08:16:14', NULL, NULL, 0, 1, 1, 'menu', '登录日志', 'login-log', 'loginLog:list', '/system/log/login-log', 'ri:login-box-line', 1, NULL, NULL, 1, 0, 0, NULL, NULL, NULL, NULL, '321200533090585415'), ('321200533556035791', NULL, '2026-06-05 08:16:14', NULL, NULL, 0, 1, 1, 'menu', '菜单日志', 'menu-log', 'menuLog:list', '/system/log/menu-log', 'ri:menu-line', 2, NULL, NULL, 1, 0, 0, NULL, NULL, NULL, NULL, '321200533090585415'), ('321200533795085477', NULL, '2026-06-05 08:16:14', NULL, NULL, 0, 1, 1, 'menu', '接口日志', 'api-log', 'apiLog:list', '/system/log/api-log', 'ri:code-line', 3, NULL, NULL, 1, 0, 0, NULL, NULL, NULL, NULL, '321200533090585415'), ('371232820192444416', 1, '2026-01-20 22:47:42', 1, '2026-01-23 14:48:48', 0, 1, 1, 'menu', '首页', '/home', 'home:list', '/dashboard/console', 'ri:dashboard-3-line', 1, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, -1), ('371233202555434287', 1, '2026-01-20 22:49:14', '373320178493861888', '2026-09-10 14:55:41.087417', 0, 1, 1, 'menu', '菜单管理', 'menu', 'system:menu:list', '/system/menu', 'ri:menu-2-line', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '371233208555634688'), ('371233208555634688', 1, '2026-01-20 22:49:14', 1, '2026-01-22 00:18:40', 0, 1, 1, 'menu', '平台管理', '/system', 'system:list', '/index/index', 'ri:settings-fill', 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, -1), ('371233705702293504', 1, '2026-01-20 22:51:13', NULL, NULL, 0, 1, 1, 'menu', '用户管理', 'user', 'userManage:list', NULL, 'ri:user-6-fill', 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '371233208555634688'), ('371233909935538176', 1, '2026-01-20 22:52:01', NULL, NULL, 0, 1, 1, 'menu', '账号管理', 'userManager', 'user:list', '/system/user', 'ri:user-fill', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '371233705702293504'), ('371234311775027200', 1, '2026-01-20 22:53:37', '373320178493861888', '2026-09-10 16:20:28.905485', 0, 1, 1, 'menu', '部门管理', 'deptManage', 'system:dept:list', '/system/dept', 'ri:contacts-book-2-fill', 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '371233705702293504'), ('371234467610198016', 1, '2026-01-20 22:54:14', '373320178493861888', '2026-09-10 16:23:48.144721', 0, 1, 1, 'menu', '角色管理', 'roleManage', 'system:role:list', '/system/role', 'ri:account-pin-circle-fill', 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '371233705702293504'), ('371234981546655744', 1, '2026-01-20 22:56:17', NULL, NULL, 0, 1, 1, 'menu', '权限管理', 'auth', 'auth:list', NULL, 'ri:mouse-line', 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '371233208555634688'), ('371235229933338624', 1, '2026-01-20 22:57:16', '373320178493861888', '2026-09-10 16:31:02.051160', 0, 1, 1, 'menu', '功能权限管理', 'menuAuth', 'system:menu-permission:list', '/system/permission/menu-permission', NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '371234981546655744'), ('371235379200229376', 1, '2026-01-20 22:57:52', '373320178493861888', '2026-09-10 17:11:12.237361', 0, 1, 0, 'menu', '数据权限管理', 'dataAuth', 'dataAuth:list', '/platform/auth/DataAuthManage', NULL, 2, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '371234981546655744'), ('371235528861384705', 1, '2026-01-22 00:14:58', '373320178493861888', '2026-09-10 14:56:42.943643', 0, 1, 1, 'menu', '字典管理', 'dict', 'system:dict:list', '/system/dict', 'ri:book-3-line', 5, '', '', 0, 0, 0, 0, 0, 0, 0, '371233208555634688'), ('371235528861384706', 1, '2026-01-22 13:17:22', '373320178493861888', '2026-09-10 14:57:14.919003', 0, 1, 1, 'menu', '字典项管理', 'dictValue', 'system:dict:list', '/system/dict-value', 'ri:book-3-line', 5, '', '/system/dict', 0, 1, 0, 0, 0, 0, 0, '371233208555634688'), ('374303658753257472', '373320178493861888', '2026-01-29 10:10:06', '373320178493861888', '2026-09-10 14:57:28.919259', 0, 1, 1, 'menu', '规则管理', 'ruleManage', 'system:rule:list', '/system/rule', 'ri:login-circle-line', 6, '', '', 0, 0, 0, 0, 0, 0, 0, '371233208555634688'), ('376567751572787200', '373320178493861888', '2026-02-04 16:06:48', NULL, NULL, 0, 1, 1, 'button', '新增', '', 'system:dept:add', '', '', 1, '', '', 0, 0, 0, 0, 0, 0, 0, '371234311775027200'), ('376567856333918208', '373320178493861888', '2026-02-04 16:07:13', NULL, NULL, 0, 1, 1, 'button', '编辑', '', 'system:dept:edit', '', '', 2, '', '', 0, 0, 0, 0, 0, 0, 0, '371234311775027200'), ('376567949967560704', '373320178493861888', '2026-02-04 16:07:36', NULL, NULL, 0, 1, 1, 'button', '删除', '', 'system:dept:delete', '', '', 3, '', '', 0, 0, 0, 0, 0, 0, 0, '371234311775027200'), ('380295457299623936', '373320178493861888', '2026-02-14 22:59:23', '373320178493861888', '2026-09-10 15:45:34.310679', 0, 1, 1, 'button', '重置密码', '', 'system:user:resetPassword', '', '', 5, '', '', 0, 0, 0, 0, 0, 0, 0, '371233909935538176'), ('445181004081651712', '373320178493861888', '2026-08-13 00:11:22.436556', '373320178493861888', '2026-08-13 00:12:22.599320', 0, NULL, 1, 'menu', '存储管理', 'oss', 'oss:list', '', 'ri:save-3-fill', 5, '', '', 0, 0, 0, 0, 0, 0, 0, '371233208555634688'), ('445184428705595392', '373320178493861888', '2026-08-13 00:24:58.930076', '373320178493861888', '2026-09-10 16:33:13.620390', 0, NULL, 1, 'menu', '存储配置', 'ossConfig', 'system:oss-config:list', '/system/oss', NULL, 1, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, '445181004081651712'), ('445347834227396608', '373320178493861888', '2026-08-13 11:14:17.844378', '373320178493861888', '2026-09-10 16:45:05.123635', 0, NULL, 1, 'menu', '文件管理', 'ossFile', 'system:oss-file:list', '/system/oss-file', '', 2, '', '', 0, 0, 0, 0, 0, 0, 0, '445181004081651712'), ('445744589603348480', '373320178493861888', '2026-08-14 13:30:51.696813', '373320178493861888', '2026-08-14 13:35:03.609687', 0, NULL, 1, 'menu', '知识库', '/knowledge-base', 'knowledge-base:list', '/index/index', 'ri:book-open-line', 3, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, -1), ('445745556159733760', '373320178493861888', '2026-08-14 13:34:42.140148', '373320178493861888', '2026-08-14 13:34:54.556871', 0, NULL, 1, 'menu', '文档管理', 'manage', 'knowledgeDocManage:list', '/system/knowledge-doc-manage', '', 1, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, '445744589603348480'), ('453360342934532096', '373320178493861888', '2026-09-04 13:53:08.784214', '373320178493861888', '2026-09-10 14:58:20.495734', 0, NULL, 1, 'menu', '租户管理', 'tenant', 'system:tenant:list', NULL, 'ri:team-line', 8, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, '371233208555634688'), ('453360711651602432', '373320178493861888', '2026-09-04 13:54:36.692742', '373320178493861888', '2026-09-10 14:58:07.802404', 0, NULL, 1, 'menu', '租户管理', 'manage', 'system:tenant:list', '/system/tenant', NULL, 2, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, '453360342934532096'), ('453361157657112576', '373320178493861888', '2026-09-04 13:56:23.028053', '373320178493861888', '2026-09-10 14:57:49.064763', 0, NULL, 1, 'menu', '租户套餐管理', 'package', 'system:tenantPackage:list', '/system/tenant-package', NULL, 1, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, '453360342934532096'), ('455559505805881344', '373320178493861888', '2026-09-10 15:31:50.070183', NULL, NULL, 0, NULL, 1, 'button', '查询', '', 'system:login-log:list', '', '', 1, '', '', 0, 0, 0, 0, 0, 0, 0, '321200533295976747'), ('455559632872321024', '373320178493861888', '2026-09-10 15:32:20.365494', NULL, NULL, 0, NULL, 1, 'button', '删除', '', 'system:login-log:delete', '', '', 2, '', '', 0, 0, 0, 0, 0, 0, 0, '321200533295976747'), ('455561256197992448', '373320178493861888', '2026-09-10 15:38:47.397174', NULL, NULL, 0, NULL, 1, 'button', '查询', '', 'system:menu-log:list', '', '', 1, '', '', 0, 0, 0, 0, 0, 0, 0, '321200533556035791'), ('455561312884011008', '373320178493861888', '2026-09-10 15:39:00.911840', NULL, NULL, 0, NULL, 1, 'button', '删除', '', 'system:menu-log:delete', '', '', 2, '', '', 0, 0, 0, 0, 0, 0, 0, '321200533556035791'), ('455561771363381248', '373320178493861888', '2026-09-10 15:40:50.221358', NULL, NULL, 0, NULL, 1, 'button', '查询', '', 'system:api-log:list', '', '', 1, '', '', 0, 0, 0, 0, 0, 0, 0, '321200533795085477'), ('455562049215049728', '373320178493861888', '2026-09-10 15:41:56.466704', NULL, NULL, 0, NULL, 1, 'button', '删除', '', 'system:api-log:delete', '', '', 2, '', '', 0, 0, 0, 0, 0, 0, 0, '321200533795085477'), ('455562556067328000', '373320178493861888', '2026-09-10 15:43:57.309700', NULL, NULL, 0, NULL, 1, 'button', '查询', '', 'system:user:list', '', '', 1, '', '', 0, 0, 0, 0, 0, 0, 0, '371233909935538176'), ('455562753010872320', '373320178493861888', '2026-09-10 15:44:44.264308', NULL, NULL, 0, NULL, 1, 'button', '新增', '', 'system:user:add', '', '', 2, '', '', 0, 0, 0, 0, 0, 0, 0, '371233909935538176'), ('455562823747809280', '373320178493861888', '2026-09-10 15:45:01.129017', NULL, NULL, 0, NULL, 1, 'button', '编辑', '', 'system:user:edit', '', '', 3, '', '', 0, 0, 0, 0, 0, 0, 0, '371233909935538176'), ('455562901120135168', '373320178493861888', '2026-09-10 15:45:19.576673', NULL, NULL, 0, NULL, 1, 'button', '删除', '', 'system:user:delete', '', '', 4, '', '', 0, 0, 0, 0, 0, 0, 0, '371233909935538176'), ('455572739552227328', '373320178493861888', '2026-09-10 16:24:25.241832', NULL, NULL, 0, NULL, 1, 'button', '编辑', NULL, 'system:role:edit', NULL, NULL, 1, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, '371234467610198016'), ('455572812117880832', '373320178493861888', '2026-09-10 16:24:42.542371', NULL, NULL, 0, NULL, 1, 'button', '新增', NULL, 'system:role:add', NULL, NULL, 2, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, '371234467610198016'), ('455572873073700864', '373320178493861888', '2026-09-10 16:24:57.075525', NULL, NULL, 0, NULL, 1, 'button', '删除', NULL, 'system:role:delete', NULL, NULL, 3, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, '371234467610198016'), ('455573597144788992', '373320178493861888', '2026-09-10 16:27:49.707394', NULL, NULL, 0, NULL, 1, 'button', '编辑权限', '', 'system:menu-permission:edit', '', '', 1, '', '', 0, 0, 0, 0, 0, 0, 0, '371235229933338624'), ('455575092917481472', '373320178493861888', '2026-09-10 16:33:46.327151', NULL, NULL, 0, NULL, 1, 'button', '新增', NULL, 'system:oss-config:add', NULL, NULL, 1, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, '445184428705595392'), ('455575157878861824', '373320178493861888', '2026-09-10 16:34:01.815306', NULL, NULL, 0, NULL, 1, 'button', '编辑', NULL, 'system:oss-config:edit', NULL, NULL, 2, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, '445184428705595392'), ('455575215248551936', '373320178493861888', '2026-09-10 16:34:15.493244', NULL, NULL, 0, NULL, 1, 'button', '删除', NULL, 'system:oss-config:delete', NULL, NULL, 3, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, '445184428705595392'), ('455575296437694464', '373320178493861888', '2026-09-10 16:34:34.850464', NULL, NULL, 0, NULL, 1, 'button', '启用', NULL, 'system:oss-config:enable', NULL, NULL, 4, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, '445184428705595392'), ('455578047519436800', '373320178493861888', '2026-09-10 16:45:30.759060', NULL, NULL, 0, NULL, 1, 'button', '上传', '', 'system:oss-file:upload', '', '', 1, '', '', 0, 0, 0, 0, 0, 0, 0, '445347834227396608'), ('455578112224964608', '373320178493861888', '2026-09-10 16:45:46.186614', NULL, NULL, 0, NULL, 1, 'button', '下载', '', 'system:oss-file:download', '', '', 2, '', '', 0, 0, 0, 0, 0, 0, 0, '445347834227396608'), ('455578172899766272', '373320178493861888', '2026-09-10 16:46:00.652644', NULL, NULL, 0, NULL, 1, 'button', '删除', '', 'system:oss-file:delete', '', '', 3, '', '', 0, 0, 0, 0, 0, 0, 0, '445347834227396608');

CREATE TABLE "p_sys_menu_log" (
  "id" bigint NOT NULL,
  "user_id" bigint,
  "user_name" character varying(64),
  "nick_name" character varying(64),
  "menu_name" character varying(128),
  "menu_path" character varying(256),
  "click_time" timestamp without time zone,
  "tenant_id" bigint,
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" smallint,
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_sys_menu_log" IS '菜单日志表';
COMMENT ON COLUMN "p_sys_menu_log"."id" IS '主键';
COMMENT ON COLUMN "p_sys_menu_log"."user_id" IS '用户ID';
COMMENT ON COLUMN "p_sys_menu_log"."user_name" IS '用户名';
COMMENT ON COLUMN "p_sys_menu_log"."nick_name" IS '用户昵称';
COMMENT ON COLUMN "p_sys_menu_log"."menu_name" IS '菜单名称';
COMMENT ON COLUMN "p_sys_menu_log"."menu_path" IS '菜单路径';
COMMENT ON COLUMN "p_sys_menu_log"."click_time" IS '点击时间';
COMMENT ON COLUMN "p_sys_menu_log"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "p_sys_menu_log"."create_id" IS '创建人';
COMMENT ON COLUMN "p_sys_menu_log"."create_time" IS '创建时间';
COMMENT ON COLUMN "p_sys_menu_log"."update_id" IS '修改人';
COMMENT ON COLUMN "p_sys_menu_log"."update_time" IS '修改时间';
COMMENT ON COLUMN "p_sys_menu_log"."delete_flag" IS '删除标识';
CREATE INDEX "idx_tenant_menu_log" ON "p_sys_menu_log" USING btree ("tenant_id");

INSERT INTO "p_sys_menu_log" ("id", "user_id", "user_name", "nick_name", "menu_name", "menu_path", "click_time", "tenant_id", "create_id", "create_time", "update_id", "update_time", "delete_flag") VALUES ('455595358989148160', '373320178493861888', 'superadmin', '超级管理员', '首页', '/home', '2026-09-10 17:54:18.131566', 1, '373320178493861888', '2026-09-10 17:54:18.139055', NULL, NULL, 0), ('455595384616345600', '373320178493861888', 'superadmin', '超级管理员', '账号管理', '/system/user/userManager', '2026-09-10 17:54:24.245166', 1, '373320178493861888', '2026-09-10 17:54:24.246166', NULL, NULL, 0), ('455595717245624320', '373320178493861888', 'superadmin', '超级管理员', '部门管理', '/system/user/deptManage', '2026-09-10 17:55:43.550071', 1, '373320178493861888', '2026-09-10 17:55:43.550071', NULL, NULL, 0), ('455595726229823488', '373320178493861888', 'superadmin', '超级管理员', '角色管理', '/system/user/roleManage', '2026-09-10 17:55:45.692197', 1, '373320178493861888', '2026-09-10 17:55:45.692197', NULL, NULL, 0), ('455595750925885440', '373320178493861888', 'superadmin', '超级管理员', '功能权限管理', '/system/auth/menuAuth', '2026-09-10 17:55:51.579166', 1, '373320178493861888', '2026-09-10 17:55:51.580671', NULL, NULL, 0), ('455595826729541632', '373320178493861888', 'superadmin', '超级管理员', '角色管理', '/system/user/roleManage', '2026-09-10 17:56:09.652143', 1, '373320178493861888', '2026-09-10 17:56:09.653194', NULL, NULL, 0), ('455595857876443136', '373320178493861888', 'superadmin', '超级管理员', '租户套餐管理', '/system/tenant/package', '2026-09-10 17:56:17.079510', 1, '373320178493861888', '2026-09-10 17:56:17.079510', NULL, NULL, 0), ('455595864323088384', '373320178493861888', 'superadmin', '超级管理员', '租户管理', '/system/tenant/manage', '2026-09-10 17:56:18.615890', 1, '373320178493861888', '2026-09-10 17:56:18.616459', NULL, NULL, 0), ('455595881909805056', '373320178493861888', 'superadmin', '超级管理员', '租户套餐管理', '/system/tenant/package', '2026-09-10 17:56:22.809264', 1, '373320178493861888', '2026-09-10 17:56:22.809264', NULL, NULL, 0), ('455596041800867840', '373320178493861888', 'superadmin', '超级管理员', '文档管理', '/knowledge-base/manage', '2026-09-10 17:57:00.930680', 1, '373320178493861888', '2026-09-10 17:57:00.930680', NULL, NULL, 0), ('455596127725379584', '373320178493861888', 'superadmin', '超级管理员', '首页', '/home', '2026-09-10 17:57:21.415387', 1, '373320178493861888', '2026-09-10 17:57:21.416405', NULL, NULL, 0);

CREATE TABLE "p_sys_oss_config" (
  "id" bigint NOT NULL,
  "config_name" character varying(100) NOT NULL,
  "endpoint" character varying(255) NOT NULL,
  "access_key" character varying(255) NOT NULL,
  "secret_key" text NOT NULL,
  "bucket_name" character varying(255) NOT NULL,
  "enable_flag" smallint NOT NULL DEFAULT 0,
  "remark" character varying(500),
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" smallint NOT NULL DEFAULT 0,
  "tenant_id" bigint,
  PRIMARY KEY ("id"),
  CONSTRAINT "ck_sys_oss_config_enable" CHECK (enable_flag = ANY (ARRAY[0, 1]))
);
COMMENT ON TABLE "p_sys_oss_config" IS '对象存储配置表';
COMMENT ON COLUMN "p_sys_oss_config"."id" IS '主键';
COMMENT ON COLUMN "p_sys_oss_config"."config_name" IS '配置名称';
COMMENT ON COLUMN "p_sys_oss_config"."endpoint" IS 'S3兼容服务地址';
COMMENT ON COLUMN "p_sys_oss_config"."access_key" IS 'Access Key';
COMMENT ON COLUMN "p_sys_oss_config"."secret_key" IS 'Secret Key（加密存储）';
COMMENT ON COLUMN "p_sys_oss_config"."bucket_name" IS 'Bucket名称';
COMMENT ON COLUMN "p_sys_oss_config"."enable_flag" IS '启用标识：0-禁用 1-启用';
COMMENT ON COLUMN "p_sys_oss_config"."remark" IS '备注';
COMMENT ON COLUMN "p_sys_oss_config"."tenant_id" IS '租户ID';
CREATE INDEX "idx_sys_oss_config_enable" ON "p_sys_oss_config" USING btree ("enable_flag", "delete_flag");
CREATE UNIQUE INDEX "uk_sys_oss_config_enabled" ON "p_sys_oss_config" USING btree (COALESCE(tenant_id, (0)::bigint)) WHERE (enable_flag = 1);
CREATE UNIQUE INDEX "uk_sys_oss_config_name" ON "p_sys_oss_config" USING btree ("tenant_id", "config_name");

CREATE TABLE "p_sys_role" (
  "id" bigint NOT NULL,
  "create_time" timestamp without time zone,
  "create_id" bigint,
  "update_time" timestamp without time zone,
  "update_id" bigint,
  "delete_flag" smallint,
  "tenant_id" bigint,
  "role_name" character varying(500),
  "role_code" character varying(255),
  "role_description" character varying(1000),
  "enable_flag" smallint,
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_sys_role" IS '角色表';
COMMENT ON COLUMN "p_sys_role"."id" IS '主键';
COMMENT ON COLUMN "p_sys_role"."create_time" IS '创建时间';
COMMENT ON COLUMN "p_sys_role"."create_id" IS '创建人id';
COMMENT ON COLUMN "p_sys_role"."update_time" IS '修改时间';
COMMENT ON COLUMN "p_sys_role"."update_id" IS '修改人id';
COMMENT ON COLUMN "p_sys_role"."delete_flag" IS '删除标识';
COMMENT ON COLUMN "p_sys_role"."tenant_id" IS '租户id';
COMMENT ON COLUMN "p_sys_role"."role_name" IS '角色名称';
COMMENT ON COLUMN "p_sys_role"."role_code" IS '角色编码';
COMMENT ON COLUMN "p_sys_role"."role_description" IS '角色说明';
COMMENT ON COLUMN "p_sys_role"."enable_flag" IS '是否启用';

INSERT INTO "p_sys_role" ("id", "create_time", "create_id", "update_time", "update_id", "delete_flag", "tenant_id", "role_name", "role_code", "role_description", "enable_flag") VALUES ('374729672067207168', '2026-01-30 14:22:56', '373320178493861888', NULL, NULL, 0, 1, '普通角色', 'normal', '普通角色', 1);

CREATE TABLE "p_sys_role_permission" (
  "id" bigint NOT NULL,
  "role_id" bigint,
  "permission_sign" character varying(500),
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_sys_role_permission" IS '角色权限表';
COMMENT ON COLUMN "p_sys_role_permission"."id" IS '主键ID';
COMMENT ON COLUMN "p_sys_role_permission"."role_id" IS '角色ID';
COMMENT ON COLUMN "p_sys_role_permission"."permission_sign" IS '权限标识';
CREATE INDEX "p_sys_role_permission_role_id_IDX" ON "p_sys_role_permission" USING btree ("role_id");

CREATE TABLE "p_sys_rule" (
  "id" bigint NOT NULL,
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" smallint,
  "enable_flag" smallint,
  "rule_code" character varying(100),
  "rule_name" character varying(255),
  "rule_value" text,
  "rule_value_type" character varying(100),
  "tenant_id" bigint,
  "remark" character varying(2000),
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_sys_rule" IS '规则表';
COMMENT ON COLUMN "p_sys_rule"."id" IS '主键ID';
COMMENT ON COLUMN "p_sys_rule"."create_id" IS '创建人id';
COMMENT ON COLUMN "p_sys_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "p_sys_rule"."update_id" IS '修改人id';
COMMENT ON COLUMN "p_sys_rule"."update_time" IS '修改时间';
COMMENT ON COLUMN "p_sys_rule"."delete_flag" IS '删除标识';
COMMENT ON COLUMN "p_sys_rule"."enable_flag" IS '是否启用';
COMMENT ON COLUMN "p_sys_rule"."rule_code" IS '规则编码';
COMMENT ON COLUMN "p_sys_rule"."rule_name" IS '规则名称';
COMMENT ON COLUMN "p_sys_rule"."rule_value" IS '规则值';
COMMENT ON COLUMN "p_sys_rule"."rule_value_type" IS '规则值类型';
COMMENT ON COLUMN "p_sys_rule"."tenant_id" IS '租户id';
COMMENT ON COLUMN "p_sys_rule"."remark" IS '备注';

INSERT INTO "p_sys_rule" ("id", "create_id", "create_time", "update_id", "update_time", "delete_flag", "enable_flag", "rule_code", "rule_name", "rule_value", "rule_value_type", "tenant_id", "remark") VALUES ('374318452109602816', '373320178493861888', '2026-01-29 11:08:53', '373320178493861888', '2026-01-29 11:05:03', 0, 1, 'system_default_pwd', '系统-默认用户密码', '123456', 'text', 1, '新增用户默认密码规则');

CREATE TABLE "p_sys_tenant" (
  "id" bigint NOT NULL,
  "tenant_code" character varying(50) NOT NULL,
  "tenant_name" character varying(100) NOT NULL,
  "package_id" bigint NOT NULL,
  "enable_flag" smallint DEFAULT 1,
  "expire_date" date,
  "remark" character varying(500),
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" smallint DEFAULT 0,
  "tenant_id" bigint,
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_sys_tenant" IS '租户表';
COMMENT ON COLUMN "p_sys_tenant"."tenant_code" IS '租户编码（唯一）';
COMMENT ON COLUMN "p_sys_tenant"."tenant_name" IS '租户名称';
COMMENT ON COLUMN "p_sys_tenant"."package_id" IS '租户套餐ID（p_sys_tenant_package.id）';
COMMENT ON COLUMN "p_sys_tenant"."enable_flag" IS '启用状态 0-禁用 1-启用';
COMMENT ON COLUMN "p_sys_tenant"."expire_date" IS '租户到期时间（到期后禁止登录）';
COMMENT ON COLUMN "p_sys_tenant"."remark" IS '备注';
COMMENT ON COLUMN "p_sys_tenant"."delete_flag" IS '删除标识 0-未删除 1-已删除';
COMMENT ON COLUMN "p_sys_tenant"."tenant_id" IS '租户id';
CREATE INDEX "idx_tenant_enable" ON "p_sys_tenant" USING btree ("enable_flag");
CREATE INDEX "idx_tenant_package_id" ON "p_sys_tenant" USING btree ("package_id");
CREATE UNIQUE INDEX "uk_tenant_code" ON "p_sys_tenant" USING btree ("tenant_code");

INSERT INTO "p_sys_tenant" ("id", "tenant_code", "tenant_name", "package_id", "enable_flag", "expire_date", "remark", "create_id", "create_time", "update_id", "update_time", "delete_flag", "tenant_id") VALUES (1, 'default', '默认租户', 1, 1, '2099-12-31', '系统默认租户', NULL, '2026-09-04 13:05:50.980687', NULL, NULL, 0, NULL);

CREATE TABLE "p_sys_tenant_package" (
  "id" bigint NOT NULL,
  "package_name" character varying(100) NOT NULL,
  "permission_signs" text,
  "enable_flag" smallint DEFAULT 1,
  "remark" character varying(500),
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" smallint DEFAULT 0,
  "tenant_id" bigint,
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_sys_tenant_package" IS '租户套餐表';
COMMENT ON COLUMN "p_sys_tenant_package"."package_name" IS '套餐名称';
COMMENT ON COLUMN "p_sys_tenant_package"."permission_signs" IS '菜单权限标识集合（逗号分隔，NULL/空=不限制）';
COMMENT ON COLUMN "p_sys_tenant_package"."enable_flag" IS '启用状态 0-禁用 1-启用';
COMMENT ON COLUMN "p_sys_tenant_package"."remark" IS '备注';
COMMENT ON COLUMN "p_sys_tenant_package"."delete_flag" IS '删除标识 0-未删除 1-已删除';
COMMENT ON COLUMN "p_sys_tenant_package"."tenant_id" IS '租户id';
CREATE INDEX "idx_tenant_package_enable" ON "p_sys_tenant_package" USING btree ("enable_flag");

INSERT INTO "p_sys_tenant_package" ("id", "package_name", "permission_signs", "enable_flag", "remark", "create_id", "create_time", "update_id", "update_time", "delete_flag", "tenant_id") VALUES (1, '默认全量套餐', NULL, 1, '系统预置套餐，不选默认视为全选', NULL, '2026-09-04 13:05:50.881725', '373320178493861888', '2026-09-10 17:56:56.714286', 0, NULL);

CREATE TABLE "p_sys_user" (
  "id" bigint NOT NULL,
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" smallint,
  "tenant_id" bigint,
  "enable_flag" smallint,
  "user_name" character varying(64),
  "nick_name" character varying(255),
  "password" character varying(500),
  "user_type" character varying(255),
  "dept_id" bigint,
  "user_status" character varying(255),
  "user_email" character varying(255),
  "user_gender" character varying(64),
  "user_phone" character varying(16),
  "user_address" character varying(500),
  "user_description" character varying(2000),
  "user_tag" character varying(255),
  "first_login_flag" smallint,
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_sys_user" IS '用户表';
COMMENT ON COLUMN "p_sys_user"."id" IS '主键ID';
COMMENT ON COLUMN "p_sys_user"."create_id" IS '创建人id';
COMMENT ON COLUMN "p_sys_user"."create_time" IS '创建时间';
COMMENT ON COLUMN "p_sys_user"."update_id" IS '修改人id';
COMMENT ON COLUMN "p_sys_user"."update_time" IS '修改时间';
COMMENT ON COLUMN "p_sys_user"."delete_flag" IS '删除标识';
COMMENT ON COLUMN "p_sys_user"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "p_sys_user"."enable_flag" IS '是否启用';
COMMENT ON COLUMN "p_sys_user"."user_name" IS '用户名';
COMMENT ON COLUMN "p_sys_user"."nick_name" IS '用户昵称';
COMMENT ON COLUMN "p_sys_user"."password" IS '密码';
COMMENT ON COLUMN "p_sys_user"."user_type" IS '用户类型';
COMMENT ON COLUMN "p_sys_user"."dept_id" IS '部门ID';
COMMENT ON COLUMN "p_sys_user"."user_status" IS '用户状态';
COMMENT ON COLUMN "p_sys_user"."user_email" IS '用户邮箱';
COMMENT ON COLUMN "p_sys_user"."user_gender" IS '性别';
COMMENT ON COLUMN "p_sys_user"."user_phone" IS '电话';
COMMENT ON COLUMN "p_sys_user"."user_address" IS '地址';
COMMENT ON COLUMN "p_sys_user"."user_description" IS '个人介绍';
COMMENT ON COLUMN "p_sys_user"."user_tag" IS '标签';
COMMENT ON COLUMN "p_sys_user"."first_login_flag" IS '是否首次登录';
CREATE INDEX "idx_tenant_user" ON "p_sys_user" USING btree ("tenant_id");

INSERT INTO "p_sys_user" ("id", "create_id", "create_time", "update_id", "update_time", "delete_flag", "tenant_id", "enable_flag", "user_name", "nick_name", "password", "user_type", "dept_id", "user_status", "user_email", "user_gender", "user_phone", "user_address", "user_description", "user_tag", "first_login_flag") VALUES ('373320178493861888', 1, '2026-01-26 17:02:07', '373320178493861888', '2026-03-09 16:42:38', 0, 1, 1, 'superadmin', '超级管理员', '$2a$10$bkPIS/d5pWKPymjvUqinXOrgCzEKQUqwBfsUB9DkwwsBOCuqHh.LO', 'superadmin', '372139251365388288', '0', '690278565@qq.com', '1', '18888888888', '山东省济南市历城区唐冶街道XXX小区', '小小码农', '码农,Java,Vue,Spring Boot', 0), (900002, NULL, '2026-09-04 13:07:55', '373320178493861888', '2026-09-10 17:55:35.950698', 0, 1, 1, 'admin', '默认租户管理员', '$2a$10$vAGUthX5xIGiSgFLOrtl2ufzZqHczVIoL.LHPoL5Q0cJ.BoktW0Te', 'admin', '372139251365388288', '0', '690278565@qq.com', '1', '15588888888', 'XX省XX市XXX街道', NULL, NULL, 1);

CREATE TABLE "p_sys_user_permission" (
  "id" bigint NOT NULL,
  "user_id" bigint,
  "permission_sign" character varying(500),
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_sys_user_permission" IS '用户权限表';
COMMENT ON COLUMN "p_sys_user_permission"."id" IS '主键ID';
COMMENT ON COLUMN "p_sys_user_permission"."user_id" IS '用户ID';
COMMENT ON COLUMN "p_sys_user_permission"."permission_sign" IS '权限标识';
CREATE INDEX "p_sys_user_permission_user_id_IDX" ON "p_sys_user_permission" USING btree ("user_id");

CREATE TABLE "p_sys_user_role" (
  "id" bigint NOT NULL,
  "user_id" bigint,
  "role_id" bigint,
  PRIMARY KEY ("id")
);
COMMENT ON TABLE "p_sys_user_role" IS '用户-角色 关联表';
COMMENT ON COLUMN "p_sys_user_role"."id" IS '主键ID';
COMMENT ON COLUMN "p_sys_user_role"."user_id" IS '用户id';
COMMENT ON COLUMN "p_sys_user_role"."role_id" IS '角色ID';
CREATE INDEX "p_sys_user_role_role_id_IDX" ON "p_sys_user_role" USING btree ("role_id");
CREATE INDEX "p_sys_user_role_user_id_IDX" ON "p_sys_user_role" USING btree ("user_id");

INSERT INTO "p_sys_user_role" ("id", "user_id", "role_id") VALUES ('455595685641543680', 900002, '374729672067207168');

CREATE TABLE "p_sys_oss_file" (
  "id" bigint NOT NULL,
  "oss_config_id" bigint NOT NULL,
  "file_name" character varying(255) NOT NULL,
  "object_key" character varying(1024) NOT NULL,
  "content_type" character varying(255),
  "file_size" bigint NOT NULL DEFAULT 0,
  "file_md5" character varying(64),
  "url" character varying(1024),
  "create_id" bigint,
  "create_time" timestamp without time zone,
  "update_id" bigint,
  "update_time" timestamp without time zone,
  "delete_flag" smallint NOT NULL DEFAULT 0,
  "tenant_id" bigint,
  PRIMARY KEY ("id"),
  CONSTRAINT "fk_sys_oss_file_config" FOREIGN KEY ("oss_config_id") REFERENCES "p_sys_oss_config"("id")
);
COMMENT ON TABLE "p_sys_oss_file" IS '对象存储上传文件信息表';
COMMENT ON COLUMN "p_sys_oss_file"."id" IS '主键';
COMMENT ON COLUMN "p_sys_oss_file"."oss_config_id" IS '对象存储配置ID';
COMMENT ON COLUMN "p_sys_oss_file"."file_name" IS '原始文件名';
COMMENT ON COLUMN "p_sys_oss_file"."object_key" IS '对象存储Key';
COMMENT ON COLUMN "p_sys_oss_file"."content_type" IS '文件类型';
COMMENT ON COLUMN "p_sys_oss_file"."file_size" IS '文件大小（字节）';
COMMENT ON COLUMN "p_sys_oss_file"."file_md5" IS '文件MD5';
COMMENT ON COLUMN "p_sys_oss_file"."url" IS '访问地址';
COMMENT ON COLUMN "p_sys_oss_file"."tenant_id" IS '租户ID';
CREATE INDEX "idx_sys_oss_file_config" ON "p_sys_oss_file" USING btree ("oss_config_id", "create_time");
CREATE INDEX "idx_sys_oss_file_create_time" ON "p_sys_oss_file" USING btree ("create_time");
CREATE UNIQUE INDEX "uk_sys_oss_file_object_key" ON "p_sys_oss_file" USING btree ("object_key");

