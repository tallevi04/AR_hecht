package com.Hecht.ar;

/**
 * Here we are taking details of the 3D Model/Object
 * */
public class object3d {

    public String object_name;
    public String object_number;
    public String object_id;
    public String object_url;
    public String object_desc;

    //Details of the 3D Model
    public object3d(String name, String next_short_code_str, String cloudAnchorId,String url,String desc) {
        this.object_name=name;
        this.object_number=next_short_code_str;
        this.object_id=cloudAnchorId;
        this.object_url=url;
        this.object_desc=desc;
    }
}
