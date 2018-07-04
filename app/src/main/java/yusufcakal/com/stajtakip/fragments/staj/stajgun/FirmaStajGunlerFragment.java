package yusufcakal.com.stajtakip.fragments.staj.stajgun;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import yusufcakal.com.stajtakip.R;
import yusufcakal.com.stajtakip.adapter.firma.StajGunlerAdapter;
import yusufcakal.com.stajtakip.pojo.Staj;
import yusufcakal.com.stajtakip.pojo.StajGun;
import yusufcakal.com.stajtakip.pojo.StajGunResim;
import yusufcakal.com.stajtakip.webservices.interfaces.FragmentListener;
import yusufcakal.com.stajtakip.webservices.interfaces.StajGunListeleListener;
import yusufcakal.com.stajtakip.webservices.services.StajGunlerService;
import yusufcakal.com.stajtakip.webservices.util.LinkUtil;
import yusufcakal.com.stajtakip.webservices.util.SessionUtil;
import yusufcakal.com.stajtakip.webservices.util.SharedPrefsUtils;

/**
 * Created by Yusuf on 21.05.2018.
 */

public class FirmaStajGunlerFragment extends Fragment implements
        StajGunListeleListener,
        AdapterView.OnItemClickListener{

    private View view;
    private FragmentListener fragmentListener;
    private final Calendar myCalendar = Calendar.getInstance();
    private Staj staj;
    private TextView tvStajGunYok;
    private ListView lvStajGunler;
    List<StajGun> stajGunList = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_firmastajgunler, container, false);

        Log.e("Kullanıcı Id", String.valueOf(SessionUtil.getUserId(getContext())));

        staj = (Staj) getArguments().get("staj");

        StajGunlerService stajGunlerService = new StajGunlerService(getContext(), this);
        stajGunlerService.stajGunler(staj.getId());

        tvStajGunYok = view.findViewById(R.id.tvStajGunYok);
        lvStajGunler = view.findViewById(R.id.lvStajGunler);
        lvStajGunler.setOnItemClickListener(this);

        return view;
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }

    };

    @Override
    public void onSuccess(String result) {
        try {

            JSONObject jsonObject = new JSONObject(result);
            boolean resultFlag = jsonObject.getBoolean("result");
            String resimYolu = jsonObject.getString("url");
            SharedPrefsUtils.setStringPreference(getContext(), LinkUtil.RESIM_YOLU, resimYolu);
            if (resultFlag){
                stajGunList = new ArrayList<>();
                JSONArray gunlerArray = jsonObject.getJSONArray("gunler");
                for (int i=0; i<gunlerArray.length(); i++){
                    JSONObject stajGun = gunlerArray.getJSONObject(i);
                    int stajGunId = stajGun.getInt("id");
                    int stajId = stajGun.getInt("staj_id");
                    String tarih = stajGun.getString("staj_tarihi");
                    String aciklama = stajGun.getString("aciklama");
                    int firmaOnay = stajGun.getInt("firma_onay");
                    int okulOnay = stajGun.getInt("okul_onay");
                    List<StajGunResim> stajGunResimList = new ArrayList<>();
                    JSONArray stajGunResimler = stajGun.getJSONArray("resimler");
                    for (int j=0; j<stajGunResimler.length(); j++){
                        JSONObject stajGunResimObject = stajGunResimler.getJSONObject(j);
                        int resimId = stajGunResimObject.getInt("id");
                        String resim = stajGunResimObject.getString("resim");
                        StajGunResim stajGunResim = new StajGunResim(resimId, resim);
                        stajGunResimList.add(stajGunResim);
                    }
                    StajGun stajGunPojo = new StajGun(stajGunId, stajId, aciklama, stajGunResimList, firmaOnay, okulOnay, tarih);
                    stajGunPojo.setImagePath(resimYolu);
                    stajGunList.add(stajGunPojo);
                }

                StajGunlerAdapter stajGunlerAdapter = new StajGunlerAdapter(getContext(), stajGunList);
                lvStajGunler.setAdapter(stajGunlerAdapter);

                if (stajGunList.size() == 0){
                    tvStajGunYok.setVisibility(View.VISIBLE);
                    lvStajGunler.setVisibility(View.GONE);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onError(VolleyError error) {
        Toast.makeText(getContext(), String.valueOf(error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentListener = (FragmentListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("stajGun", stajGunList.get(i));
        StajGunDetayFragment stajGunDetayFragment = new StajGunDetayFragment();
        stajGunDetayFragment.setArguments(bundle);
        fragmentListener.onStart(stajGunDetayFragment);
    }

}