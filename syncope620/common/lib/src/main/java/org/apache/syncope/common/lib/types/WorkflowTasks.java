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
package org.apache.syncope.common.lib.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WorkflowTasks {

    private List<String> tasks;

    public WorkflowTasks() {
        this.tasks = new ArrayList<String>();
    }

    public WorkflowTasks(final Collection<String> tasks) {
        this();
        this.tasks.addAll(tasks);
    }

    public List<String> getTasks() {
        return tasks;
    }

    public void setTasks(final List<String> tasks) {
        this.tasks = tasks;
    }
}