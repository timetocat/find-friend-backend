create database if not exists my_db;

use lyx;
-- 用户表
create table if not exists user
(
    id            bigint auto_increment comment 'id'
        primary key,
    username      varchar(256) charset utf8mb3       null comment '用户昵称',
    user_account  varchar(256)                       null comment '用户账号',
    avatar_url    varchar(1024)                      null comment '用户头像',
    gender        tinyint                            null comment '性别',
    user_password varchar(128)                       not null comment '密码',
    phone         varchar(128)                       null comment '电话',
    email         varchar(128)                       null comment '邮箱',
    user_status   tinyint  default 0                 null comment '状态 0-正常',
    user_role     tinyint  default 0                 not null comment '用户角色 0-普通用户 1-管理员',
    tags         varchar(1024) null comment '标签 json 列表',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    is_delete     tinyint  default 0                 not null comment '是否删除 0-未删除 1-删除',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '用户表';
-- 队伍表
create table if not exists team
(
    id          bigint auto_increment comment 'id' primary key,
    name        varchar(256)       not null comment '队伍名称',
    description varchar(1024) null comment '描述',
    max_num      int      default 1 not null comment '最大人数',
    expire_time  datetime null comment '过期时间',
    user_id      bigint comment '用户id（队长 id）',
    status      int      default 0 not null comment '0 - 公开，1 - 私有，2 - 加密',
    password    varchar(512) null comment '密码',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_delete    tinyint  default 0 not null comment '是否删除'
) comment '队伍';


-- 用户队伍表
-- 用户队伍关系
create table if not exists user_team
(
    id         bigint auto_increment comment 'id'
        primary key,
    user_id     bigint comment '用户id',
    team_id     bigint comment '队伍id',
    join_time   datetime null comment '加入时间',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_delete   tinyint  default 0 not null comment '是否删除'
) comment '用户队伍关系';

-- Tag表
create table if not exists tag
(
    id         bigint auto_increment comment 'id'
        primary key,
    tag_name    varchar(256) null comment '标签名称',
    user_id     bigint null comment '用户 id',
    parent_id   bigint null comment '父标签 id',
    is_parent   tinyint null comment '0 - 不是, 1 - 父标签',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_delete   tinyint  default 0 not null comment '是否删除',
    constraint uniIdx_tagName
        unique (tag_name)
) comment '标签';

create index idx_userId
    on tag (user_id);

-- 好友申请管理表
create table friends
(
    id         bigint auto_increment comment 'id'
        primary key,
    from_id     bigint                             not null comment '发送申请的用户id',
    receive_id  bigint                             null comment '接收申请的用户id ',
    is_read     tinyint  default 0                 not null comment '是否已读(0-未读 1-已读)',
    status     tinyint  default 0                 not null comment '申请状态 默认0 （0-未通过 1-已同意 2-已过期 3-已撤销）',
    remark     varchar(128)                       null comment '好友申请备注信息',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null,
    is_delete   tinyint  default 0                 not null comment '是否删除'
)
    comment '好友申请管理表';

-- 消息表
create table chat
(
    id          bigint auto_increment comment 'id'
        primary key,
    team_id     bigint                             null comment '队伍id（群聊）',
    from_id     bigint                             not null comment '发送者id',
    to_id       bigint                             null comment '接收者id（私聊）',
    content     text                               null comment '内容',
    scope       tinyint  default 1                 null comment '消息类(作用域)(0-私聊，1-群聊)',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint  default 0                 not null comment '是否删除(0-未删除，1-删除)'
)
    comment '消息表';

create table user_friends
(
    id bigint auto_increment comment 'id' primary key ,
    user_id bigint not null comment '用户id',
    friend_ids varchar(256) null comment '好友id列表',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint  default 0                 not null comment '是否删除(0-未删除，1-删除)'
)
    comment '用户好友表';
