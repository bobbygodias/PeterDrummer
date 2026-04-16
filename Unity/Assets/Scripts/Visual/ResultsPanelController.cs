using PeterDrummer.Core;
using UnityEngine;
using UnityEngine.UI;

namespace PeterDrummer.Visual
{
    /// <summary>
    /// Exibe resultados finais quando a música termina.
    /// </summary>
    public class ResultsPanelController : MonoBehaviour
    {
        [SerializeField] private SongConductor conductor;
        [SerializeField] private ScoreSystem scoreSystem;
        [SerializeField] private GameObject panelRoot;
        [SerializeField] private Text scoreText;
        [SerializeField] private Text comboText;
        [SerializeField] private Text accuracyText;
        [SerializeField] private Text countsText;

        private void OnEnable()
        {
            conductor.OnSongEnded += ShowResults;
        }

        private void OnDisable()
        {
            conductor.OnSongEnded -= ShowResults;
        }

        private void Start()
        {
            if (panelRoot != null)
            {
                panelRoot.SetActive(false);
            }
        }

        private void ShowResults()
        {
            if (panelRoot != null)
            {
                panelRoot.SetActive(true);
            }

            if (scoreText != null) scoreText.text = $"Score: {scoreSystem.Score}";
            if (comboText != null) comboText.text = $"Best Combo: {scoreSystem.BestCombo}";
            if (accuracyText != null) accuracyText.text = $"Accuracy: {scoreSystem.AccuracyPercent:0.00}%";

            if (countsText != null)
            {
                countsText.text =
                    $"Perfect: {scoreSystem.PerfectCount}\n" +
                    $"Good: {scoreSystem.GoodCount}\n" +
                    $"Miss: {scoreSystem.MissCount}";
            }
        }
    }
}
