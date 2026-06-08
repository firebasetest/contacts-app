const Dashboard = () => {
    const [data, setData] = useState([]);
    const [selectedStatus, setSelectedStatus] = useState(null);
    const [detailedList, setDetailedList] = useState([]);

    const handleBarClick = (data, index) => {
        const status = data.activePayload[0].payload.status;
        setSelectedStatus(status);
        // Fetch detailed contacts for this status
        fetch(`/api/v1/contacts?status=${status}`)
            .then(res => res.json())
            .then(setDetailedList);
    };

    return (
        <div className="dashboard-container">
            <BarChart data={data} onClick={handleBarClick}>
                {/* ... existing chart props ... */}
                <Bar dataKey="count" fill="#8884d8" />
            </BarChart>

            {selectedStatus && (
                <div className="drilldown-table">
                    <h3>Viewing: {selectedStatus}</h3>
                    <table>
                        {detailedList.map(contact => (
                            <tr key={contact.id}>
                                <td>{contact.name}</td>
                                <td>{contact.email}</td>
                            </tr>
                        ))}
                    </table>
                </div>
            )}
        </div>
    );
};