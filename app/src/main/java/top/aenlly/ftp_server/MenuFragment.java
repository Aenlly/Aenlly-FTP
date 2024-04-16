package top.aenlly.ftp_server;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import top.aenlly.ftp_server.databinding.FragmentMenuBinding;

/**
 * A fragment representing a list of Items.
 */
public class MenuFragment extends Fragment {

    private FragmentMenuBinding binding;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MenuFragment() {
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentMenuBinding.inflate(inflater, container, false);

        binding.tvFtpServer.setOnClickListener(view->{
            NavHostFragment.findNavController(MenuFragment.this)
                    .navigate(R.id.tv_ftpServer);
        });

        toAuthorLink();

        return binding.getRoot();
    }

    @SuppressLint("ResourceAsColor")
    void toAuthorLink(){
        SpannableString spannableString = new SpannableString("关于");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // 在这里执行链接跳转的操作，例如打开一个网页
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Aenlly/FTP-Server/tree/main"));
                startActivity(intent);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false); // 去除下划线
            }
        };

        spannableString.setSpan(clickableSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvAbout.setText(spannableString);
        binding.tvAbout.setMovementMethod(LinkMovementMethod.getInstance());
    }
}