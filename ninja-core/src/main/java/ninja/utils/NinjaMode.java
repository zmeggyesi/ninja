/**
 * Copyright (C) 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ninja.utils;

public enum NinjaMode {

    prod(NinjaConstant.MODE_PROD), 
    dev(NinjaConstant.MODE_DEV), 
    test(NinjaConstant.MODE_TEST);

    private String mode;

    NinjaMode(String mode) {
        this.mode = mode;
    }

    public String toString() {
        return mode;
    }

}
