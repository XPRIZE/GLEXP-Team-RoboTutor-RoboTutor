/**
 Copyright(c) 2015-2017 Kevin Willows
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package cmu.xprize.comp_clickmask;


public class CExclusion {


    public String   type;
    public int      x;
    public int      y;
    public int      radius;


    public CExclusion() {}

    public CExclusion(String _type, int _x, int _y, int _radius) {

        type   = _type;
        x      = _x;
        y      = _y;
        radius = _radius;
    }

}
