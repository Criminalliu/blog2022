package cn.yinong.api;

import cn.yinong.constant.Types;
import cn.yinong.constant.WebConst;
import cn.yinong.model.AttAchDomain;
import cn.yinong.model.UserDomain;
import cn.yinong.service.attach.AttAchService;
import cn.yinong.utils.TaleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

@Component
public class LocalService {

    @Autowired
    private AttAchService attAchService;

    @Value("${root.path}")
    private String rootPath;

    @Value("${allow.suffix}")
    private String allowSuffix;

    public AttAchDomain localUpLoad(MultipartFile file, HttpServletRequest request) throws IOException {

        String filename = file.getOriginalFilename();

        //文件名,如spring
        String name = filename.substring(0, filename.indexOf("."));

        //文件后缀,如.jpeg
        String suffix = filename.substring(filename.lastIndexOf("."));

        if (allowSuffix.indexOf(suffix) == -1) {
            return null;
        }

        //创建年月日文件夹
        Calendar date = Calendar.getInstance();
        File dateDirs = new File(date.get(Calendar.YEAR)
                + File.separator + (date.get(Calendar.MONTH) + 1));

        //目标文件
        File descFile = new File(rootPath + File.separator + dateDirs + File.separator + filename);

        int i = 1;
        //若文件存在重命名
        String newFilename = filename;
        while (descFile.exists()) {
            newFilename = name + "(" + i + ")" + suffix;
            String parentPath = descFile.getParent();
            descFile = new File(parentPath + File.separator + dateDirs + File.separator + newFilename);
            i++;
        }

        //判断目标文件所在的目录是否存在
        if (!descFile.getParentFile().exists()) {
            //如果目标文件所在的目录不存在，则创建父目录
            descFile.getParentFile().mkdirs();
        }
        String desc = descFile.toString().replace("\\","/");
        desc = desc.substring(desc.lastIndexOf("site/"));

        AttAchDomain attAch = new AttAchDomain();
        HttpSession session = request.getSession();
        UserDomain sessionUser = (UserDomain) session.getAttribute(WebConst.LOGIN_SESSION_KEY);
        attAch.setAuthorId(sessionUser.getUid());
        attAch.setFtype(TaleUtils.isImage(file.getInputStream()) ? Types.IMAGE.getType() : Types.FILE.getType());
        attAch.setFname(name);
        attAch.setFkey("/" +desc);
        attAchService.addAttAch(attAch);

        try {
            file.transferTo(descFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return attAch;
    }

}
