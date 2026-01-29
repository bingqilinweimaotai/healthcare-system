/**
 * AI 智能问诊平台后端。
 * <p>
 * 分层结构：
 * <ul>
 *   <li><b>controller</b> - 接口层，接收请求、参数校验、调用 service、返回结果</li>
 *   <li><b>service</b> - 业务层，事务、业务流程、调用 mapper</li>
 *   <li><b>mapper</b> - 数据访问层（替代原 JPA Repository），MyBatis Mapper 接口 + XML，封装 SQL</li>
 *   <li><b>entity</b> - 表对应实体（POJO）</li>
 *   <li><b>dto</b> - 请求/响应 DTO</li>
 *   <li><b>config</b> - 配置类（Sa-Token、WebSocket、Security、全局异常、初始化等）</li>
 * </ul>
 */
package com.healthcare;
