/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.igti.cep;

import java.util.Date;

public class TemperatureAlert {
    private int rackID;
    private double temperature1;
    private double temperature2;
    private double temperature3;
    private Date time1;
    private Date time2;
    private Date time3;

    public TemperatureAlert(int rackID, double temperature1, double temperature2, double temperature3,
    		Date time1, Date time2, Date time3) {
        this.rackID = rackID;
        this.temperature1 = temperature1;
        this.temperature2 = temperature2;
        this.temperature3 = temperature3;
        this.time1 = time1;
        this.time2 = time2;
        this.time3 = time3;
    }

    public TemperatureAlert() {
        this(-1, 0.0, 0.0, 0.0, new Date(), new Date(), new Date());
    }

    public void setRackID(int rackID) {
        this.rackID = rackID;
    }

    public int getRackID() {
        return rackID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TemperatureAlert) {
            TemperatureAlert other = (TemperatureAlert) obj;
            return rackID == other.rackID;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return rackID;
    }

    @Override
    public String toString() {
        return "TemperatureAlert(" + getRackID() + "," + this.temperature1 + "," + this.temperature2 + "," 
        			+ this.temperature3 + "," + this.time1 + "," + this.time2 + "," + this.time3 + ")";
    }
}
