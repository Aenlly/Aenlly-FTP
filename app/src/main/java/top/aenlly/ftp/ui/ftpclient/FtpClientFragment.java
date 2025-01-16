package top.aenlly.ftp.ui.ftpclient;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.jetbrains.annotations.NotNull;
import top.aenlly.ftp.databinding.FragmentFtpClientBinding;
import top.aenlly.ftp.ui.dialog.FtpClientDialog;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FtpClientFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FtpClientFragment extends Fragment {


    private Context context;

    private FragmentFtpClientBinding binding;
    public FtpClientFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FtpClientFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FtpClientFragment newInstance(String param1, String param2) {
        FtpClientFragment fragment = new FtpClientFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentFtpClientBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = view.getContext();
        registerForBtn();
    }

    void registerForBtn() {
        // 弹窗
        FtpClientDialog ftpClientDialog = new FtpClientDialog(context);
        binding.btnClientNew.setOnClickListener(view1-> ftpClientDialog.show());
    }

}