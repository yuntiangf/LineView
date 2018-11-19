package com.example.lineview;

import java.util.ArrayList;

import com.example.lineview2.LineView2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity{
	private LineView2 lineView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        lineView = (LineView2) (findViewById(R.id.lineview));
        lineView.updateStationList(new ArrayList<String>() {
            {
                add("老电力大楼");
                add("医药公司");
                add("依江楼");
//                add("老人民医院前门");
//                add("潇洒楼");
//                add("劳动路口");
//                add("迎宾路口");
//                add("圆通寺");
//                add("杨梅山");
//                add("富春江一桥");
//                add("上杭埠");
//                add("交警队");
//                add("乔林村");
//                add("长途汽车站");
//                add("桐庐商贸城");
//                add("青山工业园区");
            }
        });
        
//        lineView.setSelectItem(5);
        lineView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        lineView.updateCurBus(new int[]{1,6});
//        lineView.updateCurBus2(new int[]{3,9});
//        lineView.setCurStation(5);
    }
}
