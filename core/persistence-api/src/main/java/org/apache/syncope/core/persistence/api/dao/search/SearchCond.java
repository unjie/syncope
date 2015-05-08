/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.core.persistence.api.dao.search;

import java.util.List;

public class SearchCond extends AbstractSearchCond {

    private static final long serialVersionUID = 661560782247499526L;

    public enum Type {

        LEAF,
        NOT_LEAF,
        AND,
        OR

    }

    private Type type;

    private SubjectCond subjectCond;

    private AttributeCond attributeCond;

    private GroupCond groupCond;

    private RoleCond roleCond;

    private ResourceCond resourceCond;

    private SearchCond leftNodeCond;

    private SearchCond rightNodeCond;

    public static SearchCond getLeafCond(final AttributeCond attributeCond) {
        SearchCond nodeCond = new SearchCond();

        nodeCond.type = Type.LEAF;
        if (attributeCond instanceof SubjectCond) {
            nodeCond.subjectCond = (SubjectCond) attributeCond;
        } else {
            nodeCond.attributeCond = attributeCond;
        }

        return nodeCond;
    }

    public static SearchCond getLeafCond(final GroupCond groupCond) {
        SearchCond nodeCond = new SearchCond();

        nodeCond.type = Type.LEAF;
        nodeCond.groupCond = groupCond;

        return nodeCond;
    }

    public static SearchCond getLeafCond(final RoleCond roleCond) {
        SearchCond nodeCond = new SearchCond();

        nodeCond.type = Type.LEAF;
        nodeCond.roleCond = roleCond;

        return nodeCond;
    }

    public static SearchCond getLeafCond(final ResourceCond resourceCond) {
        SearchCond nodeCond = new SearchCond();

        nodeCond.type = Type.LEAF;
        nodeCond.resourceCond = resourceCond;

        return nodeCond;
    }

    public static SearchCond getNotLeafCond(final AttributeCond attributeCond) {
        SearchCond nodeCond = getLeafCond(attributeCond);
        nodeCond.type = Type.NOT_LEAF;
        return nodeCond;
    }

    public static SearchCond getNotLeafCond(final GroupCond groupCond) {
        SearchCond nodeCond = getLeafCond(groupCond);
        nodeCond.type = Type.NOT_LEAF;
        return nodeCond;
    }

    public static SearchCond getNotLeafCond(final RoleCond roleCond) {
        SearchCond nodeCond = getLeafCond(roleCond);
        nodeCond.type = Type.NOT_LEAF;
        return nodeCond;
    }

    public static SearchCond getNotLeafCond(final ResourceCond resourceCond) {
        SearchCond nodeCond = getLeafCond(resourceCond);
        nodeCond.type = Type.NOT_LEAF;
        return nodeCond;
    }

    public static SearchCond getNotLeafCond(final SearchCond nodeCond) {
        nodeCond.type = Type.NOT_LEAF;
        return nodeCond;
    }

    public static SearchCond getAndCond(final SearchCond leftCond, final SearchCond rightCond) {
        SearchCond nodeCond = new SearchCond();

        nodeCond.type = Type.AND;
        nodeCond.leftNodeCond = leftCond;
        nodeCond.rightNodeCond = rightCond;

        return nodeCond;
    }

    public static SearchCond getAndCond(final List<SearchCond> conditions) {
        if (conditions.size() > 2) {
            SearchCond removed = conditions.remove(0);
            return getAndCond(removed, getAndCond(conditions));
        } else {
            return getAndCond(conditions.get(0), conditions.get(1));
        }
    }

    public static SearchCond getOrCond(final SearchCond leftCond, final SearchCond rightCond) {
        SearchCond nodeCond = new SearchCond();

        nodeCond.type = Type.OR;
        nodeCond.leftNodeCond = leftCond;
        nodeCond.rightNodeCond = rightCond;

        return nodeCond;
    }

    public static SearchCond getOrCond(final List<SearchCond> conditions) {
        if (conditions.size() > 2) {
            SearchCond removed = conditions.remove(0);
            return getOrCond(removed, getOrCond(conditions));
        } else {
            return getOrCond(conditions.get(0), conditions.get(1));
        }
    }

    public SubjectCond getSubjectCond() {
        return subjectCond;
    }

    public void setSubjectCond(final SubjectCond subjectCond) {
        this.subjectCond = subjectCond;
    }

    public AttributeCond getAttributeCond() {
        return attributeCond;
    }

    public void setAttributeCond(final AttributeCond attributeCond) {
        this.attributeCond = attributeCond;
    }

    public GroupCond getGroupCond() {
        return groupCond;
    }

    public void setGroupCond(final GroupCond groupCond) {
        this.groupCond = groupCond;
    }

    public RoleCond getRoleCond() {
        return roleCond;
    }

    public void setRoleCond(final RoleCond roleCond) {
        this.roleCond = roleCond;
    }

    public ResourceCond getResourceCond() {
        return resourceCond;
    }

    public void setResourceCond(final ResourceCond resourceCond) {
        this.resourceCond = resourceCond;
    }

    public SearchCond getLeftNodeCond() {
        return leftNodeCond;
    }

    public void setLeftNodeCond(final SearchCond leftNodeCond) {
        this.leftNodeCond = leftNodeCond;
    }

    public SearchCond getRightNodeCond() {
        return rightNodeCond;
    }

    public void setRightNodeCond(final SearchCond rightNodeCond) {
        this.rightNodeCond = rightNodeCond;
    }

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    @Override
    public boolean isValid() {
        boolean isValid = false;

        if (type == null) {
            return isValid;
        }

        switch (type) {
            case LEAF:
            case NOT_LEAF:
                isValid = (subjectCond != null || attributeCond != null
                        || groupCond != null || roleCond != null || resourceCond != null)
                        && (subjectCond == null || subjectCond.isValid())
                        && (attributeCond == null || attributeCond.isValid())
                        && (groupCond == null || groupCond.isValid())
                        && (roleCond == null || roleCond.isValid())
                        && (resourceCond == null || resourceCond.isValid());
                break;

            case AND:
            case OR:
                isValid = (leftNodeCond == null || rightNodeCond == null)
                        ? false
                        : leftNodeCond.isValid() && rightNodeCond.isValid();
                break;

            default:
        }

        return isValid;
    }
}
