using PeterDrummer.Data;
using PeterDrummer.InputSystem;
using UnityEngine;

namespace PeterDrummer.Core
{
    /// <summary>
    /// Sistema simples de score/combos para primeira versão testável.
    /// </summary>
    public class ScoreSystem : MonoBehaviour
    {
        [SerializeField] private PlayerInputController inputController;
        [SerializeField] private float scoreMultiplier = 1f;

        public int Score { get; private set; }
        public int Combo { get; private set; }
        public int BestCombo { get; private set; }
        public int PerfectCount { get; private set; }
        public int GoodCount { get; private set; }
        public int MissCount { get; private set; }

        public int TotalJudged => PerfectCount + GoodCount + MissCount;
        public float AccuracyPercent => TotalJudged == 0
            ? 0f
            : ((PerfectCount * 1f + GoodCount * 0.5f) / TotalJudged) * 100f;

        public HitJudgement LastJudgement { get; private set; }
        public double LastErrorMs { get; private set; }

        private void OnEnable()
        {
            inputController.OnJudgement += OnJudgement;
        }

        private void OnDisable()
        {
            inputController.OnJudgement -= OnJudgement;
        }

        public void ConfigureScoreMultiplier(float multiplier)
        {
            scoreMultiplier = Mathf.Max(0.1f, multiplier);
        }

        public void ResetRun()
        {
            Score = 0;
            Combo = 0;
            BestCombo = 0;
            PerfectCount = 0;
            GoodCount = 0;
            MissCount = 0;
            LastErrorMs = 0d;
            LastJudgement = HitJudgement.Miss;
        }

        private void OnJudgement(DrumLane lane, HitJudgement judgement, double errorMs)
        {
            LastJudgement = judgement;
            LastErrorMs = errorMs;

            switch (judgement)
            {
                case HitJudgement.Perfect:
                    PerfectCount++;
                    Combo++;
                    Score += Mathf.RoundToInt((300 + Combo * 2) * scoreMultiplier);
                    break;

                case HitJudgement.Good:
                    GoodCount++;
                    Combo++;
                    Score += Mathf.RoundToInt((150 + Combo) * scoreMultiplier);
                    break;

                case HitJudgement.Miss:
                    MissCount++;
                    Combo = 0;
                    break;
            }

            if (Combo > BestCombo)
            {
                BestCombo = Combo;
            }
        }
    }
}
