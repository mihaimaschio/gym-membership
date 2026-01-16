using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Windows.Forms;
using GymMembershipClient.Models;
using GymMembershipClient.Services;

namespace GymMembershipClient.Forms
{
    public class MemberDashboardForm : Form
    {
        private readonly ApiClient _apiClient;
        private readonly MemberService _memberService;
        private readonly ClassService _classService;
        private readonly SubscriptionService _subscriptionService;

        private Member _currentMember;
        private Label _welcomeLabel;
        private TabControl _tabControl;
        
        // Subscription Controls
        private Label _subTypeLabel;
        private Label _subPriceLabel;
        private Label _subDateLabel;
        private Label _subStatusLabel;

        // Classes Controls
        private DataGridView _classesGrid;
        private ComboBox _dayFilterBox;
        private Button _enrollButton;

        // Enrollments Controls
        private DataGridView _enrollmentsGrid;
        private Button _cancelButton;

        public MemberDashboardForm(ApiClient apiClient, string userEmail)
        {
            _apiClient = apiClient;
            _memberService = new MemberService(apiClient);
            _classService = new ClassService(apiClient);
            _subscriptionService = new SubscriptionService(apiClient);

            InitializeComponent();
            LoadData(userEmail);
        }

        private void InitializeComponent()
        {
            this.Text = "Member Dashboard";
            this.Size = new Size(1000, 700);
            this.StartPosition = FormStartPosition.CenterScreen;

            _welcomeLabel = new Label { Location = new Point(20, 20), AutoSize = true, Font = new Font(this.Font.FontFamily, 14, FontStyle.Bold) };
            this.Controls.Add(_welcomeLabel);

            _tabControl = new TabControl { Location = new Point(20, 60), Size = new Size(940, 580) };
            
            // Tab 1: Subscription
            TabPage subTab = new TabPage("My Subscription");
            InitializeSubscriptionTab(subTab);
            _tabControl.TabPages.Add(subTab);

            // Tab 2: Available Classes
            TabPage classesTab = new TabPage("Available Classes");
            InitializeClassesTab(classesTab);
            _tabControl.TabPages.Add(classesTab);

            // Tab 3: My Enrollments
            TabPage enrollTab = new TabPage("My Enrollments");
            InitializeEnrollmentsTab(enrollTab);
            _tabControl.TabPages.Add(enrollTab);

            this.Controls.Add(_tabControl);
        }

        private void InitializeSubscriptionTab(TabPage tab)
        {
            _subTypeLabel = new Label { Location = new Point(20, 20), AutoSize = true, Font = new Font(this.Font, FontStyle.Regular) };
            _subPriceLabel = new Label { Location = new Point(20, 50), AutoSize = true };
            _subDateLabel = new Label { Location = new Point(20, 80), AutoSize = true };
            _subStatusLabel = new Label { Location = new Point(20, 110), AutoSize = true, Font = new Font(this.Font, FontStyle.Bold) };

            tab.Controls.Add(_subTypeLabel);
            tab.Controls.Add(_subPriceLabel);
            tab.Controls.Add(_subDateLabel);
            tab.Controls.Add(_subStatusLabel);
        }

        private void InitializeClassesTab(TabPage tab)
        {
            Label filterLabel = new Label { Text = "Filter by Day:", Location = new Point(20, 20), AutoSize = true };
            _dayFilterBox = new ComboBox { Location = new Point(120, 17), Width = 150, DropDownStyle = ComboBoxStyle.DropDownList };
            _dayFilterBox.Items.AddRange(new object[] { "All", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY" });
            _dayFilterBox.SelectedIndex = 0;
            _dayFilterBox.SelectedIndexChanged += (s, e) => FilterClasses();

            _enrollButton = new Button { Text = "Enroll Selected", Location = new Point(300, 15), Width = 120 };
            _enrollButton.Click += EnrollButton_Click;

            _classesGrid = new DataGridView { Location = new Point(20, 60), Size = new Size(900, 480), AutoGenerateColumns = false, SelectionMode = DataGridViewSelectionMode.FullRowSelect, MultiSelect = false };
            _classesGrid.Columns.Add(new DataGridViewTextBoxColumn { HeaderText = "ID", DataPropertyName = "Id", Width = 50 });
            _classesGrid.Columns.Add(new DataGridViewTextBoxColumn { HeaderText = "Type", DataPropertyName = "Type", Width = 150 });
            _classesGrid.Columns.Add(new DataGridViewTextBoxColumn { HeaderText = "Day", DataPropertyName = "DayOfWeek", Width = 100 });
            _classesGrid.Columns.Add(new DataGridViewTextBoxColumn { HeaderText = "Time", DataPropertyName = "Time", Width = 100 });
            _classesGrid.Columns.Add(new DataGridViewTextBoxColumn { HeaderText = "Spots", DataPropertyName = "AvailableSpots", Width = 80 });

            tab.Controls.Add(filterLabel);
            tab.Controls.Add(_dayFilterBox);
            tab.Controls.Add(_enrollButton);
            tab.Controls.Add(_classesGrid);
        }

        private void InitializeEnrollmentsTab(TabPage tab)
        {
            _cancelButton = new Button { Text = "Cancel Enrollment", Location = new Point(20, 20), Width = 150 };
            _cancelButton.Click += CancelButton_Click;

            _enrollmentsGrid = new DataGridView { Location = new Point(20, 60), Size = new Size(900, 480), AutoGenerateColumns = false, SelectionMode = DataGridViewSelectionMode.FullRowSelect, MultiSelect = false };
            _enrollmentsGrid.Columns.Add(new DataGridViewTextBoxColumn { HeaderText = "ID", DataPropertyName = "Id", Width = 50 });
            _enrollmentsGrid.Columns.Add(new DataGridViewTextBoxColumn { HeaderText = "Class Type", DataPropertyName = "ClassType", Width = 150 });
            _enrollmentsGrid.Columns.Add(new DataGridViewTextBoxColumn { HeaderText = "Day", DataPropertyName = "Day", Width = 100 });
            _enrollmentsGrid.Columns.Add(new DataGridViewTextBoxColumn { HeaderText = "Time", DataPropertyName = "Time", Width = 100 });

            tab.Controls.Add(_cancelButton);
            tab.Controls.Add(_enrollmentsGrid);
        }

        private async void LoadData(string email)
        {
            try
            {
                _currentMember = await _memberService.GetMemberByEmailAsync(email);
                _welcomeLabel.Text = $"Welcome, {_currentMember.FirstName} {_currentMember.LastName}!";

                UpdateSubscriptionUI();
                await LoadClasses();
                UpdateEnrollmentsUI();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error loading data: {ex.Message}", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void UpdateSubscriptionUI()
        {
            if (_currentMember.Subscription != null)
            {
                _subTypeLabel.Text = $"Type: {_currentMember.Subscription.Type}";
                _subPriceLabel.Text = $"Price: {_currentMember.Subscription.Price} RON";
                _subDateLabel.Text = $"Valid: {_currentMember.Subscription.StartDate:d} - {_currentMember.Subscription.EndDate:d}";
                _subStatusLabel.Text = _currentMember.Subscription.Active ? "ACTIVE" : "EXPIRED";
                _subStatusLabel.ForeColor = _currentMember.Subscription.Active ? Color.Green : Color.Red;
            }
            else
            {
                _subTypeLabel.Text = "No active subscription.";
                _subPriceLabel.Text = "";
                _subDateLabel.Text = "";
                _subStatusLabel.Text = "";
            }
        }

        private List<GymClass> _allClasses;

        private async System.Threading.Tasks.Task LoadClasses()
        {
            _allClasses = await _classService.GetAvailableClassesAsync();
            FilterClasses();
        }

        private void FilterClasses()
        {
            if (_allClasses == null) return;

            string selectedDay = _dayFilterBox.SelectedItem.ToString();
            var filtered = selectedDay == "All" ? _allClasses : _allClasses.Where(c => c.DayOfWeek == selectedDay).ToList();
            _classesGrid.DataSource = filtered;
        }

        private void UpdateEnrollmentsUI()
        {
            if (_currentMember.Enrollments != null)
            {
                var viewModels = _currentMember.Enrollments.Select(e => new 
                { 
                    Id = e.Id,
                    ClassType = e.GymClass?.Type,
                    Day = e.GymClass?.DayOfWeek,
                    Time = e.GymClass?.Time
                }).ToList();
                _enrollmentsGrid.DataSource = viewModels;
            }
        }

        private async void EnrollButton_Click(object sender, EventArgs e)
        {
            if (_classesGrid.SelectedRows.Count == 0) return;
            var selectedClass = (GymClass)_classesGrid.SelectedRows[0].DataBoundItem;

            try
            {
                await _classService.EnrollMemberAsync(selectedClass.Id, _currentMember.Id);
                MessageBox.Show("Enrolled successfully!", "Success", MessageBoxButtons.OK, MessageBoxIcon.Information);
                
                // Refresh data
                _currentMember = await _memberService.GetMemberByEmailAsync(_currentMember.Email);
                await LoadClasses();
                UpdateEnrollmentsUI();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Enrollment failed: {ex.Message}", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private async void CancelButton_Click(object sender, EventArgs e)
        {
            if (_enrollmentsGrid.SelectedRows.Count == 0) return;
            
            // Get ID from the anonymous object
            long enrollmentId = (long)_enrollmentsGrid.SelectedRows[0].Cells[0].Value;

            try
            {
                await _classService.CancelEnrollmentAsync(enrollmentId);
                MessageBox.Show("Enrollment cancelled!", "Success", MessageBoxButtons.OK, MessageBoxIcon.Information);

                // Refresh data
                _currentMember = await _memberService.GetMemberByEmailAsync(_currentMember.Email);
                await LoadClasses();
                UpdateEnrollmentsUI();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Cancellation failed: {ex.Message}", "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
    }
}
