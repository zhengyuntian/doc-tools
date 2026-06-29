package io.renren.common.utils;

import java.util.List;

/**
 * 树形节点接口
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
public interface TreeNode {
    
    /**
     * 获取节点ID
     */
    Long getId();
    
    /**
     * 获取父节点ID
     */
    Long getPid();
    
    /**
     * 获取子节点列表
     */
    List<?> getChildren();
    
    /**
     * 设置子节点列表
     */
    void setChildren(List<?> list);
}