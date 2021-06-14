package dorox.app.srcid;

import org.springframework.util.StringUtils;

public class CommonSrcId implements SrcId{
    @Override
    public String getSrcId() {
        return null;
    }

    //客户可以传完整的接入码（上游接入码+平台扩展位+自定义），或者只传平台扩展位+自定义，或者只传自定义
    //拼成完整srcId,传到上游处理
//    public String srcId(String srcId, String upSpCode, String downPortCode){
//
//        if( StringUtils.isEmpty(srcId) ){
//            srcId = "";
//        }
//
//        String downSpCode = getDownSpcode();
//        if( StringUtils.isEmpty(downSpCode) ){
//            downSpCode = "";
//        }
//        String res = "";
//        String vspcode = getDownVspcode();//虚拟接入号
//
//        if(StringUtils.isEmpty(vspcode)){//未分配虚拟接入号
//            if(srcId.startsWith(upSpCode)){//如果下游传了完整接入号
//                res = srcId;
//            }else if(srcId.startsWith(downSpCode)){
//                res = upSpCode + srcId;
//            }else{
//                res = upSpCode + downSpCode + srcId;
//            }
//            if(res.length()>20){res = res.substring(0,20);}
//            return res;
//        }else{
//            if(srcId.startsWith(vspcode)){//如果下游传了完整接入号,并且是给他的虚拟接入号
//                res = srcId.replace(vspcode, upSpCode);
//            }else if(srcId.startsWith(downSpCode)){
//                res = upSpCode + srcId;
//            }else{
//                res = upSpCode + downSpCode + srcId;
//            }
//            if(res.length()>20){res = res.substring(0,20);}
//            return res;
//        }
//    }
}
