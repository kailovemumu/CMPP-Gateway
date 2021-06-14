package dorox.app.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class ManageConfiguration {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private BlackFlush blackFlush;
    @Autowired
    private MobileInfoFlush mobileInfoFlush;
    @Autowired
    private MobileRouteFlush mobileRouteFlush;
    @Autowired
    private PortabilityNumberFlush portabilityNumberFlush;
    @Autowired
    private ManageDownPort manageDownPort;

    public void start() {
//    黑名单
        blackFlush.loadBlacks();
//        省，市 信息
        mobileInfoFlush.loadMobileInfos();
//        携号转网信息
        portabilityNumberFlush.loadPortabilityNumber();
//        发送给通道层信息
        mobileRouteFlush.loadMobileRoute();
//          下游信息
        manageDownPort.scanAllDownPort();


    }

}
