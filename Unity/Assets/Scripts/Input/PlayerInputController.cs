using System;
using PeterDrummer.Core;
using PeterDrummer.Data;
using PeterDrummer.Rhythm;
using UnityEngine;

namespace PeterDrummer.InputSystem
{
    /// <summary>
    /// Mapeia botões/toques para lanes e valida hit por janela temporal.
    /// </summary>
    public class PlayerInputController : MonoBehaviour
    {
        [SerializeField] private SongConductor conductor;
        [SerializeField] private HitZone kickZone;
        [SerializeField] private HitZone snareZone;
        [SerializeField] private HitZone hihatZone;
        [SerializeField] private double perfectWindowSec = 0.045;
        [SerializeField] private double goodWindowSec = 0.090;

        public event Action<DrumLane> OnLanePlayed;
        public event Action<string> OnHitFeedback;

        public void PressKick() => TryHit(kickZone, DrumLane.Kick);
        public void PressSnare() => TryHit(snareZone, DrumLane.Snare);
        public void PressHiHat() => TryHit(hihatZone, DrumLane.HiHat);

        private void Update()
        {
            // Atalhos úteis para testes no editor.
            if (UnityEngine.Input.GetKeyDown(KeyCode.A)) PressKick();
            if (UnityEngine.Input.GetKeyDown(KeyCode.S)) PressSnare();
            if (UnityEngine.Input.GetKeyDown(KeyCode.D)) PressHiHat();
        }

        private void TryHit(HitZone zone, DrumLane lane)
        {
            if (!conductor.IsPlaying) return;

            OnLanePlayed?.Invoke(lane);

            double now = conductor.CurrentSongTimeSec;
            NoteObject note = zone.GetClosest(now);

            if (note == null)
            {
                OnHitFeedback?.Invoke("MISS");
                return;
            }

            double error = Math.Abs(note.TargetSongTimeSec - now);

            if (error <= perfectWindowSec)
            {
                zone.Remove(note);
                Destroy(note.gameObject);
                OnHitFeedback?.Invoke("PERFECT");
                return;
            }

            if (error <= goodWindowSec)
            {
                zone.Remove(note);
                Destroy(note.gameObject);
                OnHitFeedback?.Invoke("GOOD");
                return;
            }

            OnHitFeedback?.Invoke("MISS");
        }
    }
}
