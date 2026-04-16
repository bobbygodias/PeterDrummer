using System;
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
        public event Action OnSongEnded;

        private bool _songEndedRaised;

        public double CurrentSongTimeSec
        {
            get
            {
                if (!IsPlaying) return 0d;
                return AudioSettings.dspTime - SongStartDspTime;
            }
        }

        private void Update()
        {
            if (!IsPlaying || _songEndedRaised || musicSource.clip == null) return;

            if (CurrentSongTimeSec >= musicSource.clip.length)
            {
                _songEndedRaised = true;
                IsPlaying = false;
                OnSongEnded?.Invoke();
            }
        }

        public void PrepareAndPlay(AudioClip clip)
        {
            musicSource.clip = clip;
            SongStartDspTime = AudioSettings.dspTime + startDelaySec;
            _songEndedRaised = false;
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
