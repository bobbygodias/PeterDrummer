using UnityEngine;

namespace PeterDrummer.Core
{
    /// <summary>
    /// Relógio central do jogo. Toda lógica de sincronização usa DSP time.
    /// </summary>
    public class SongConductor : MonoBehaviour
    {
        [SerializeField] private AudioSource musicSource;
        [SerializeField] private float startDelaySec = 0.15f;

        public double SongStartDspTime { get; private set; }
        public bool IsPlaying { get; private set; }

        public double CurrentSongTimeSec
        {
            get
            {
                if (!IsPlaying) return 0d;
                return AudioSettings.dspTime - SongStartDspTime;
            }
        }

        public void PrepareAndPlay(AudioClip clip)
        {
            musicSource.clip = clip;
            SongStartDspTime = AudioSettings.dspTime + startDelaySec;
            musicSource.PlayScheduled(SongStartDspTime);
            IsPlaying = true;
        }

        public void StopSong()
        {
            musicSource.Stop();
            IsPlaying = false;
        }
    }
}
