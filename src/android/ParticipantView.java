package cordova.plugin.videocall.ParticipantView;/*
 * Copyright (C) 2019 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.AttrRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.twilio.video.VideoScaleType;
import com.twilio.video.VideoTextureView;
import com.twilio.video.VideoTrack;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.ionic.starter.R;
import tvi.webrtc.VideoFrame;
import tvi.webrtc.VideoSink;

public abstract class ParticipantView extends FrameLayout implements VideoSink {

    private static final VideoScaleType DEFAULT_VIDEO_SCALE_TYPE = VideoScaleType.ASPECT_FIT;

  public  String identity = "";
  public  int state = State.NO_VIDEO;
  public  boolean mirror = false;
  public int scaleType = DEFAULT_VIDEO_SCALE_TYPE.ordinal();

    public VideoTrack videoTrack;
  public ConstraintLayout videoLayout;
  public TextView videoIdentity;
  public  VideoTextureView videoView;
  public  RelativeLayout selectedLayout;
  public  ImageView stubImage;
  public  @Nullable ImageView networkQualityLevelImg;
  public  TextView selectedIdentity;
  public @Nullable ImageView audioToggle;
  public  @Nullable ImageView pinImage;

    public ParticipantView(@NonNull Context context) {
        super(context);
        initParams(context, null);
    }

    public ParticipantView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initParams(context, attrs);
    }

    public ParticipantView(
            @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParams(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ParticipantView(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initParams(context, attrs);
    }

    public void setIdentity(String identity) {
        this.identity = identity;
        videoIdentity.setText(identity);
        selectedIdentity.setText(identity);
    }

    public void setState(int state) {
        this.state = state;
        switch (state) {
            case State.SWITCHED_OFF:
            case State.VIDEO:
                videoState();
                break;
            case State.NO_VIDEO:
            case State.SELECTED:
                videoLayout.setVisibility(GONE);
                videoIdentity.setVisibility(GONE);
                videoView.setVisibility(GONE);

                selectedLayout.setVisibility(VISIBLE);
                stubImage.setVisibility(VISIBLE);
                selectedIdentity.setVisibility(VISIBLE);
                break;
            default:
                break;
        }
    }

    private void videoState() {
        selectedLayout.setVisibility(GONE);
        stubImage.setVisibility(GONE);
        selectedIdentity.setVisibility(GONE);

        videoLayout.setVisibility(VISIBLE);
        videoIdentity.setVisibility(VISIBLE);
        videoView.setVisibility(VISIBLE);
    }

    public void setMirror(boolean mirror) {
        this.mirror = mirror;
        videoView.setMirror(this.mirror);
    }

    public void setScaleType(int scaleType) {
        this.scaleType = scaleType;
        videoView.setVideoScaleType(VideoScaleType.values()[this.scaleType]);
    }

    public void setMuted(boolean muted) {
        if (audioToggle != null) audioToggle.setVisibility(muted ? VISIBLE : GONE);
    }

    public void setPinned(boolean pinned) {
        if (pinImage != null) pinImage.setVisibility(pinned ? VISIBLE : GONE);
    }

    @Override
    public void onFrame(VideoFrame videoFrame) {
        videoView.onFrame(videoFrame);
    }

    void initParams(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray stylables =
                    context.getTheme()
                            .obtainStyledAttributes(attrs, R.styleable.ParticipantView, 0, 0);

            // obtain identity
            int identityResId = stylables.getResourceId(R.styleable.ParticipantView_identity, -1);
            identity = (identityResId != -1) ? context.getString(identityResId) : "";

            // obtain state
            state =
                    stylables.getInt(
                            R.styleable.ParticipantView_state, State.NO_VIDEO);

            // obtain mirror
            mirror = stylables.getBoolean(R.styleable.ParticipantView_mirror, false);

            // obtain scale type
            scaleType =
                    stylables.getInt(
                            R.styleable.ParticipantView_type, DEFAULT_VIDEO_SCALE_TYPE.ordinal());

            stylables.recycle();
        }
    }

    @IntDef({
        State.VIDEO,
        State.NO_VIDEO,
        State.SELECTED,
        State.SWITCHED_OFF
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
        int VIDEO = 0;
        int NO_VIDEO = 1;
        int SELECTED = 2;
        int SWITCHED_OFF = 3;
    }
}
